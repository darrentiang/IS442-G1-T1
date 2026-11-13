package com.studybuddy.service;

import com.studybuddy.model.ActivityEvent;
import com.studybuddy.model.ActivityType;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.ActivityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps each study group's activity feed: who joined, who left, and which sessions were proposed
 * and confirmed. It hears about members as a GroupEventListener, so the group services never
 * mention it. Like every listener it never throws: a feed problem is logged and the group action
 * that caused it carries on.
 */
@Service
public class ActivityFeedService implements GroupEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityFeedService.class);

    private final ActivityEventRepository eventRepository;
    private final int feedSize;

    /** Creates the service; the number of rows a feed shows comes from application.properties. */
    public ActivityFeedService(ActivityEventRepository eventRepository, Environment environment) {
        this.eventRepository = eventRepository;
        this.feedSize = environment.getRequiredProperty("studybuddy.activity.feed-size", Integer.class);
    }

    /** Records nothing: a new group's feed starts empty, and its leader is shown on the group page. */
    @Override
    public void onGroupCreated(StudyGroup group) {
        // Nothing to record; ActivityType has no "group created" kind.
    }

    /** Records that a student joined the group. */
    @Override
    public void onMemberJoined(StudyGroup group, Student student) {
        record(group, ActivityType.MEMBER_JOINED, nameOf(student) + " joined the group");
    }

    /** Records that a student left or was removed from the group. */
    @Override
    public void onMemberLeft(StudyGroup group, Student student) {
        record(group, ActivityType.MEMBER_LEFT, nameOf(student) + " left the group");
    }

    /** Records nothing: a closed group's page already shows a "This group is closed" banner. */
    @Override
    public void onGroupClosed(StudyGroup group) {
        // Nothing to record; ActivityType has no "group closed" kind.
    }

    /**
     * Adds one row to a group's feed, stamped with the current time. Never throws: a failure is
     * logged, so it can never block the action that called it.
     *
     * @param group the group whose feed gets the row
     * @param type the kind of action that happened
     * @param text the sentence shown in the feed
     */
    public void record(StudyGroup group, ActivityType type, String text) {
        try {
            if (group == null || type == null || text == null) {
                return;
            }
            eventRepository.save(new ActivityEvent(group, type, text, LocalDateTime.now()));
        } catch (RuntimeException e) {
            LOG.warn("Could not record activity for group {}: {}", group == null ? null : group.getId(), e.getMessage());
        }
    }

    /** Returns this group's most recent feed rows, newest first, up to the configured feed size. */
    public List<ActivityEvent> recentFor(String groupId) {
        List<ActivityEvent> all = eventRepository.findByGroup(groupId);
        List<ActivityEvent> recent = new ArrayList<>();
        for (int i = all.size() - 1; i >= 0 && recent.size() < feedSize; i--) {
            recent.add(all.get(i));
        }
        return recent;
    }

    /** Returns the student's name, or a neutral word if it is missing. */
    private String nameOf(Student student) {
        return student == null || student.getName() == null ? "A student" : student.getName();
    }
}
