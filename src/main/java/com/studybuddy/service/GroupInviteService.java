package com.studybuddy.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.GroupInvite;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.NotGroupLeaderException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.Visibility;
import com.studybuddy.repository.GroupInviteRepository;

/** Creates, resets and checks the secret invite links of private study groups. */
@Service
public class GroupInviteService {

    private static final String TOKEN_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 10;
    private static final String INVALID_LINK = "This invite link is not valid or was reset";

    private final GroupInviteRepository inviteRepository;
    private final StudyGroupService groupService;
    private final JoinRequestService requestService;
    private final SecureRandom random = new SecureRandom();

    /** Creates the service with its storage contract and the group and join request rules (constructor injection). */
    public GroupInviteService(GroupInviteRepository inviteRepository, StudyGroupService groupService,
            JoinRequestService requestService) {
        this.inviteRepository = inviteRepository;
        this.groupService = groupService;
        this.requestService = requestService;
    }

    /** Returns the private group's invite, creating one the first time; leader only. */
    public GroupInvite inviteFor(StudyGroup group, String leaderId) {
        requireLeaderOfPrivateGroup(group, leaderId);
        GroupInvite invite = inviteRepository.findByGroup(group.getId());
        if (invite == null) {
            invite = new GroupInvite(group.getId(), newToken());
            inviteRepository.save(invite);
        }
        return invite;
    }

    /** Replaces the group's invite with a new token so the old link stops working; leader only. */
    public GroupInvite resetInvite(String groupId, String leaderId) {
        StudyGroup group = groupService.findById(groupId);
        requireLeaderOfPrivateGroup(group, leaderId);
        GroupInvite invite = new GroupInvite(group.getId(), newToken());
        inviteRepository.save(invite);
        return invite;
    }

    /** Returns the group this token invites to, or null for an unknown or reset token. */
    public StudyGroup findGroupByToken(String token) {
        if (token == null) {
            return null;
        }
        GroupInvite invite = inviteRepository.findByToken(token);
        if (invite == null) {
            return null;
        }
        return groupService.findById(invite.getGroupId());
    }

    /** Sends a join request to the leader of the group this token invites to, under the usual join rules. */
    public JoinRequest requestToJoinByInvite(Student student, String token) {
        StudyGroup group = findGroupByToken(token);
        if (group == null) {
            throw new BusinessRuleException(INVALID_LINK);
        }
        return requestService.requestToJoinInvited(student, group);
    }

    /** Throws unless the group exists, is private and is led by this student. */
    private void requireLeaderOfPrivateGroup(StudyGroup group, String leaderId) {
        if (group == null) {
            throw new BusinessRuleException("This group could not be found");
        }
        if (!groupService.isLeader(group, leaderId)) {
            throw new NotGroupLeaderException("Only the group leader can manage the invite link");
        }
        if (group.getVisibility() != Visibility.PRIVATE) {
            throw new BusinessRuleException("Only private groups have an invite link");
        }
    }

    /** Returns a random token that no other group is using. */
    private String newToken() {
        String token;
        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                builder.append(TOKEN_CHARACTERS.charAt(random.nextInt(TOKEN_CHARACTERS.length())));
            }
            token = builder.toString();
        } while (inviteRepository.findByToken(token) != null);
        return token;
    }
}
