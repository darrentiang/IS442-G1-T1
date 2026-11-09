package com.studybuddy.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.DiscordAccount;
import com.studybuddy.model.DiscordChannel;
import com.studybuddy.model.DiscordLoginAttempt;
import com.studybuddy.model.NotGroupLeaderException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.DiscordAccountRepository;
import com.studybuddy.repository.DiscordChannelRepository;
import com.studybuddy.repository.DiscordLoginAttemptRepository;
import com.studybuddy.repository.StudyGroupRepository;

/**
 * Business rules for each study group's private Discord channel: a channel and role per group, and
 * only members who connected their Discord account get the role. It hears group changes as a
 * GroupEventListener, so the group services never mention Discord. Discord failures never block a
 * group action: they are logged, and the leader can set the channel up again later.
 */
@Service
public class DiscordService implements GroupEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordService.class);

    private final DiscordGateway gateway;
    private final DiscordChannelRepository channelRepository;
    private final DiscordAccountRepository accountRepository;
    private final DiscordLoginAttemptRepository attemptRepository;
    private final StudyGroupRepository groupRepository;

    /** Creates the service with the gateway and repositories it uses (constructor injection). */
    public DiscordService(DiscordGateway gateway, DiscordChannelRepository channelRepository,
            DiscordAccountRepository accountRepository, DiscordLoginAttemptRepository attemptRepository,
            StudyGroupRepository groupRepository) {
        this.gateway = gateway;
        this.channelRepository = channelRepository;
        this.accountRepository = accountRepository;
        this.attemptRepository = attemptRepository;
        this.groupRepository = groupRepository;
    }

    /**
     * Returns what this student should see in the group chat section of the group page.
     *
     * @param group the group being viewed
     * @param studentId the viewer's id, or null for an administrator
     * @return HIDDEN unless Discord is set up and the viewer is a member
     */
    public DiscordChatStatus statusFor(StudyGroup group, String studentId) {
        if (!gateway.isConfigured() || studentId == null || !isMember(group, studentId)) {
            return DiscordChatStatus.HIDDEN;
        }
        if (channelRepository.findByGroup(group.getId()) == null) {
            return DiscordChatStatus.NO_CHANNEL;
        }
        if (accountRepository.findByStudent(studentId) == null) {
            return DiscordChatStatus.NOT_CONNECTED;
        }
        return DiscordChatStatus.READY;
    }

    /** Returns the web address that opens the group's channel in Discord, or null if it has none. */
    public String chatUrl(StudyGroup group) {
        DiscordChannel channel = channelRepository.findByGroup(group.getId());
        if (channel == null) {
            return null;
        }
        return gateway.channelUrl(channel.getChannelId());
    }

    /**
     * Creates the group's Discord channel, for groups made before Discord was set up or whose
     * channel failed to be created. Members who already connected Discord get access straight away.
     *
     * @throws BusinessRuleException if Discord is off, the group is closed or already has a channel,
     *         the student is not the leader, or Discord fails
     */
    public void setUpChannel(StudyGroup group, String leaderId) {
        requireConfigured();
        if (group.getLeader() == null || !group.getLeader().getId().equals(leaderId)) {
            throw new NotGroupLeaderException("Only the group leader can set up the Discord channel");
        }
        if (!group.isOpen()) {
            throw new BusinessRuleException("Reopen the group before setting up its Discord channel");
        }
        if (channelRepository.findByGroup(group.getId()) != null) {
            throw new BusinessRuleException("This group already has a Discord channel");
        }
        try {
            createChannel(group);
        } catch (DiscordException exception) {
            LOG.warn("Could not set up the Discord channel for group {}: {}", group.getId(), exception.getMessage());
            throw new BusinessRuleException("Discord could not be reached. Try again in a moment.");
        }
    }

    /**
     * Starts connecting a member's Discord account: remembers a one-time state value and returns the
     * Discord page to send them to.
     *
     * @throws BusinessRuleException if Discord is off or the student is not a member of the group
     */
    public String startConnect(StudyGroup group, Student student) {
        requireConfigured();
        if (!isMember(group, student.getId())) {
            throw new BusinessRuleException("Join the group before connecting Discord");
        }
        String state = UUID.randomUUID().toString();
        attemptRepository.save(new DiscordLoginAttempt(state, student.getId(), group.getId()));
        return gateway.loginUrl(state);
    }

    /**
     * Finishes connecting: checks the state value, links the Discord account to the student, adds
     * them to the server and gives them the role of every group they belong to.
     *
     * @param state the state value Discord sent back
     * @param code the code Discord sent back
     * @param studentId the signed-in student, who must be the one who started connecting
     * @return the id of the group page to return to
     * @throws BusinessRuleException if the state is unknown or belongs to someone else, or Discord fails
     */
    public String finishConnect(String state, String code, String studentId) {
        requireConfigured();
        DiscordLoginAttempt attempt = attemptRepository.take(state);
        if (attempt == null || !attempt.getStudentId().equals(studentId)) {
            throw new BusinessRuleException("This Discord sign-in has expired. Please try connecting again.");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("Discord did not finish signing in. Please try again.");
        }
        try {
            DiscordUser user = gateway.exchangeCode(code);
            gateway.addToServer(user);
            accountRepository.save(new DiscordAccount(studentId, user.getId()));
            for (StudyGroup group : groupRepository.findByMember(studentId)) {
                DiscordChannel channel = channelRepository.findByGroup(group.getId());
                if (channel != null) {
                    gateway.grantRole(user.getId(), channel.getRoleId());
                }
            }
        } catch (DiscordException exception) {
            LOG.warn("Could not connect Discord for student {}: {}", studentId, exception.getMessage());
            throw new BusinessRuleException("Discord could not be reached. Try connecting again in a moment.");
        }
        return attempt.getGroupId();
    }

    /**
     * Forgets a Discord sign-in the student cancelled, so its state value cannot be used later.
     *
     * @return the id of the group page to return to, or null if the state is unknown or not theirs
     */
    public String cancelConnect(String state, String studentId) {
        DiscordLoginAttempt attempt = attemptRepository.take(state);
        if (attempt == null || !attempt.getStudentId().equals(studentId)) {
            return null;
        }
        return attempt.getGroupId();
    }

    /** Creates the new group's channel; the leader gets access if they already connected Discord. */
    @Override
    public void onGroupCreated(StudyGroup group) {
        if (!gateway.isConfigured()) {
            return;
        }
        try {
            createChannel(group);
        } catch (DiscordException exception) {
            LOG.warn("Group {} was created without a Discord channel: {}", group.getId(), exception.getMessage());
        }
    }

    /** Gives the new member access to the group's channel, if both exist on Discord. */
    @Override
    public void onMemberJoined(StudyGroup group, Student student) {
        DiscordChannel channel = channelRepository.findByGroup(group.getId());
        DiscordAccount account = accountRepository.findByStudent(student.getId());
        if (!gateway.isConfigured() || channel == null || account == null) {
            return;
        }
        try {
            gateway.grantRole(account.getDiscordUserId(), channel.getRoleId());
        } catch (DiscordException exception) {
            LOG.warn("Could not give {} access to group {} on Discord: {}", student.getId(), group.getId(),
                    exception.getMessage());
        }
    }

    /** Takes away the former member's access to the group's channel. */
    @Override
    public void onMemberLeft(StudyGroup group, Student student) {
        DiscordChannel channel = channelRepository.findByGroup(group.getId());
        DiscordAccount account = accountRepository.findByStudent(student.getId());
        if (!gateway.isConfigured() || channel == null || account == null) {
            return;
        }
        try {
            gateway.revokeRole(account.getDiscordUserId(), channel.getRoleId());
        } catch (DiscordException exception) {
            LOG.warn("Could not remove {} from group {} on Discord: {}", student.getId(), group.getId(),
                    exception.getMessage());
        }
    }

    /** Deletes the closed group's channel and role; reopening the group lets the leader set up a new one. */
    @Override
    public void onGroupClosed(StudyGroup group) {
        DiscordChannel channel = channelRepository.findByGroup(group.getId());
        if (!gateway.isConfigured() || channel == null) {
            return;
        }
        channelRepository.deleteByGroup(group.getId());
        try {
            gateway.deleteChannel(channel);
        } catch (DiscordException exception) {
            LOG.warn("Could not delete the Discord channel of closed group {}: {}", group.getId(),
                    exception.getMessage());
        }
    }

    /** Creates the channel and role, saves the link, and gives access to members who connected Discord. */
    private void createChannel(StudyGroup group) {
        DiscordChannel channel = gateway.createPrivateChannel(group.getId(),
                group.getCourse().getCode() + " " + group.getName());
        channelRepository.save(channel);
        for (Student member : group.getMembers()) {
            DiscordAccount account = accountRepository.findByStudent(member.getId());
            if (account != null) {
                gateway.grantRole(account.getDiscordUserId(), channel.getRoleId());
            }
        }
    }

    /** Throws if the Discord settings are missing. */
    private void requireConfigured() {
        if (!gateway.isConfigured()) {
            throw new BusinessRuleException("The Discord group chat is not set up for this app");
        }
    }

    /** Returns true if the student is the leader or one of the group's members. */
    private boolean isMember(StudyGroup group, String studentId) {
        if (group.getLeader() != null && group.getLeader().getId().equals(studentId)) {
            return true;
        }
        for (Student member : group.getMembers()) {
            if (member.getId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }
}
