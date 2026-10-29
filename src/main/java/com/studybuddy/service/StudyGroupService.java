package com.studybuddy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.Visibility;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Business rules for creating, editing, closing and looking up study groups. */
@Service
public class StudyGroupService {

    private static final int MAX_NAME_LENGTH = 60;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MIN_GROUP_SIZE = 2;
    private static final int MAX_GROUP_SIZE = 10;

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
        Map<String, String> fieldErrors = new HashMap<>();

        String name = trimOrEmpty(form.getName());
        if (name.isEmpty()) {
            fieldErrors.put("name", "Group name is required");
        } else if (name.length() > MAX_NAME_LENGTH) {
            fieldErrors.put("name", "Group name must be at most " + MAX_NAME_LENGTH + " characters");
        }

        String description = trimOrEmpty(form.getDescription());
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            fieldErrors.put("description", "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        // The course is fixed after creation, so it is only checked on create.
        if (existing == null) {
            String courseCode = trimOrEmpty(form.getCourseCode());
            if (courseCode.isEmpty() || courseRepository.findByCode(courseCode) == null) {
                fieldErrors.put("courseCode", "Choose a course");
            }
        }

        List<String> goals = form.getGoals();
        if (goals == null || goals.isEmpty()) {
            fieldErrors.put("goals", "Choose at least one study goal");
        } else {
            for (String goal : goals) {
                if (toGoal(goal) == null) {
                    fieldErrors.put("goals", "Choose study goals from the list");
                    break;
                }
            }
        }

        if (toMode(form.getMode()) == null) {
            fieldErrors.put("mode", "Choose a study mode");
        }

        if (toVisibility(form.getVisibility()) == null) {
            fieldErrors.put("visibility", "Choose who can see this group");
        }

        Integer maxSize = parseWholeNumber(form.getMaxSize());
        if (maxSize == null) {
            fieldErrors.put("maxSize", "Max size must be a whole number");
        } else if (maxSize < MIN_GROUP_SIZE || maxSize > MAX_GROUP_SIZE) {
            fieldErrors.put("maxSize", "Max size must be between " + MIN_GROUP_SIZE + " and " + MAX_GROUP_SIZE);
        } else if (existing != null && maxSize < existing.getMembers().size()) {
            fieldErrors.put("maxSize", "Max size can't be below the current "
                    + existing.getMembers().size() + " members");
        }

        try {
            AvailabilityGrid.parse(form.getAvailabilityText());
        } catch (IllegalArgumentException e) {
            fieldErrors.put("availabilityText", "Availability: " + e.getMessage());
        }

        return fieldErrors;
    }

    /** Creates a group led by this student (also its first member), open, and saves it. */
    public StudyGroup createGroup(Student leader, GroupForm form) {
        StudyGroup group = new StudyGroup(groupRepository.nextId());
        group.setCourse(courseRepository.findByCode(trimOrEmpty(form.getCourseCode())));
        applyForm(group, form);
        group.setLeader(leader);
        group.getMembers().add(leader);
        group.setOpen(true);
        groupRepository.save(group);
        return group;
    }

    /** Applies the form to an existing group; the course never changes after creation. */
    public void updateGroup(StudyGroup group, GroupForm form) {
        applyForm(group, form);
        groupRepository.save(group);
    }

    /** Closes the group and declines all its pending requests; only the leader may do this. */
    public void closeGroup(String groupId, String leaderId) {
        StudyGroup group = findById(groupId);
        if (group == null) {
            throw new IllegalStateException("This group could not be found");
        }
        if (!isLeader(group, leaderId)) {
            throw new IllegalStateException("Only the group leader can close this group");
        }
        if (!group.isOpen()) {
            throw new IllegalStateException("This group is already closed");
        }
        group.setOpen(false);
        groupRepository.save(group);

        for (JoinRequest request : requestRepository.findByGroup(groupId)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                request.decline();
                requestRepository.save(request);
            }
        }
    }

    /** Reopens a closed group for joining; only the leader may do this. */
    public void reopenGroup(String groupId, String leaderId) {
        StudyGroup group = findById(groupId);
        if (group == null) {
            throw new IllegalStateException("This group could not be found");
        }
        if (!isLeader(group, leaderId)) {
            throw new IllegalStateException("Only the group leader can reopen this group");
        }
        if (group.isOpen()) {
            throw new IllegalStateException("This group is already open");
        }
        group.setOpen(true);
        groupRepository.save(group);
    }

    /** Returns true when members.size() >= maxSize; the leader is in members, so the leader counts toward maxSize. */
    public boolean isFull(StudyGroup group) {
        return group.isFull();
    }

    /** Returns all groups, in seed order. */
    public List<StudyGroup> findAll() {
        return groupRepository.findAll();
    }

    /** Returns the group with this id, or null if there is no such group. */
    public StudyGroup findById(String id) {
        return groupRepository.findById(id);
    }

    /** Returns the groups this student leads. */
    public List<StudyGroup> findLedBy(String studentId) {
        return groupRepository.findByLeader(studentId);
    }

    /** Returns the groups this student belongs to but does not lead. */
    public List<StudyGroup> findJoinedBy(String studentId) {
        List<StudyGroup> result = new ArrayList<>();
        for (StudyGroup group : groupRepository.findByMember(studentId)) {
            if (!isLeader(group, studentId)) {
                result.add(group);
            }
        }
        return result;
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
        if (group == null || studentId == null) {
            return false;
        }
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

    /** Copies the editable form values onto the group; assumes the form already passed validate. */
    private void applyForm(StudyGroup group, GroupForm form) {
        group.setName(trimOrEmpty(form.getName()));
        group.setDescription(trimOrEmpty(form.getDescription()));
        group.setGoals(toGoals(form.getGoals()));
        group.setMode(toMode(form.getMode()));
        group.setVisibility(toVisibility(form.getVisibility()));
        group.setMaxSize(parseWholeNumber(form.getMaxSize()));
        group.setAvailability(AvailabilityGrid.parse(form.getAvailabilityText()));
    }

    /** Returns the text trimmed, or an empty string when it is null. */
    private String trimOrEmpty(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    /** Returns the text as a whole number, or null if it is not one. */
    private Integer parseWholeNumber(String text) {
        try {
            return Integer.parseInt(trimOrEmpty(text));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns the StudyGoal with this name, or null if there is none. */
    private StudyGoal toGoal(String text) {
        for (StudyGoal goal : StudyGoal.values()) {
            if (goal.name().equals(text)) {
                return goal;
            }
        }
        return null;
    }

    /** Converts goal names to a set of StudyGoal, skipping unknown names. */
    private Set<StudyGoal> toGoals(List<String> names) {
        Set<StudyGoal> goals = new HashSet<>();
        if (names == null) {
            return goals;
        }
        for (String name : names) {
            StudyGoal goal = toGoal(name);
            if (goal != null) {
                goals.add(goal);
            }
        }
        return goals;
    }

    /** Returns the StudyMode with this name, or null if there is none. */
    private StudyMode toMode(String text) {
        for (StudyMode mode : StudyMode.values()) {
            if (mode.name().equals(text)) {
                return mode;
            }
        }
        return null;
    }

    /** Returns the Visibility with this name, or null if there is none. */
    private Visibility toVisibility(String text) {
        for (Visibility visibility : Visibility.values()) {
            if (visibility.name().equals(text)) {
                return visibility;
            }
        }
        return null;
    }
}
