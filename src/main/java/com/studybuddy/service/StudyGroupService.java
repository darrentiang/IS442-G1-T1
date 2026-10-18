package com.studybuddy.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Business rules for creating, editing, closing and looking up study groups. */
@Service
public class StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final JoinRequestRepository requestRepository;
    private final CourseRepository courseRepository;

    /** Creates the service with the repositories it uses (constructor injection). */
    public StudyGroupService(StudyGroupRepository groupRepository,
                             JoinRequestRepository requestRepository,
                             CourseRepository courseRepository) {
        this.groupRepository = groupRepository;
        this.requestRepository = requestRepository;
        this.courseRepository = courseRepository;
    }

    /** Returns field name to message for every invalid field; empty when valid. existing is null on create. */
    public Map<String, String> validate(GroupForm form, StudyGroup existing) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Creates a group led by this student (also its first member), open, and saves it. */
    public StudyGroup createGroup(Student leader, GroupForm form) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Applies the form to an existing group; the course never changes after creation. */
    public void updateGroup(StudyGroup group, GroupForm form) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Closes the group and declines all its pending requests; only the leader may do this. */
    public void closeGroup(String groupId, String leaderId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Reopens a closed group for joining; only the leader may do this. */
    public void reopenGroup(String groupId, String leaderId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Returns true when members.size() >= maxSize; the leader is in members, so the leader counts toward maxSize. */
    public boolean isFull(StudyGroup group) {
        return group.getMembers().size() >= group.getMaxSize();
    }

    /** Returns all groups, in seed order. */
    public List<StudyGroup> findAll() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Returns the group with this id, or null if there is no such group. */
    public StudyGroup findById(String id) {
        return groupRepository.findById(id);
    }

    /** Returns the groups this student leads. */
    public List<StudyGroup> findLedBy(String studentId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Returns the groups this student belongs to. */
    public List<StudyGroup> findJoinedBy(String studentId) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /** Returns true when this student is the group's leader; false if group or studentId is null. */
    public boolean isLeader(StudyGroup group, String studentId) {
        if (group == null || studentId == null) {
            return false;
        }
        return group.getLeader() != null && group.getLeader().getId().equals(studentId);
    }

    /** Returns true if the student is the leader OR in members (the leader counts as a member). */
    public boolean isMember(StudyGroup group, String studentId) {
        if (isLeader(group, studentId)) {
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
