package com.studybuddy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A standing many-person match for one course. The page around it is a
 * coordination dashboard: heatmap, sessions, feed. Never a chat.
 */
public class StudyGroup {

    private final String id;
    private String name;
    private String description;
    private Course course;
    private Student leader;
    private List<Student> members = new ArrayList<>();
    private int maxSize;
    private Visibility visibility;
    private Set<StudyGoal> goals = EnumSet.noneOf(StudyGoal.class);
    private StudyMode mode;
    private boolean open;
    private AvailabilityGrid availability = new AvailabilityGrid();

    /** Creates a group shell; the Leader team's service fills in the rest. */
    public StudyGroup(String id) {
        this.id = id;
    }

    /** Returns the unique group id. */
    public String getId() {
        return id;
    }

    /** Returns the group name. */
    public String getName() {
        return name;
    }

    /** Changes the group name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the group description. */
    public String getDescription() {
        return description;
    }

    /** Changes the group description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns the course this group studies. */
    public Course getCourse() {
        return course;
    }

    /** Changes the course this group studies. */
    public void setCourse(Course course) {
        this.course = course;
    }

    /** Returns the student holding the leader role. */
    public Student getLeader() {
        return leader;
    }

    /** Transfers the leader role to another student. */
    public void setLeader(Student leader) {
        this.leader = leader;
    }

    /** Returns the group members as a read-only view; use addMember and removeMember to change them. */
    public List<Student> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** Returns true if this student is in members (the leader is always in members). */
    public boolean hasMember(Student student) {
        for (Student member : members) {
            if (member.getId().equals(student.getId())) {
                return true;
            }
        }
        return false;
    }

    /** Returns true when the group has maxSize members; the leader counts toward maxSize. */
    public boolean isFull() {
        return members.size() >= maxSize;
    }

    /** Adds a student to members; throws GroupFullException if full, BusinessRuleException if already a member. */
    public void addMember(Student student) {
        if (hasMember(student)) {
            throw new BusinessRuleException(student.getName() + " is already in this group");
        }
        if (isFull()) {
            throw new GroupFullException("This group is full");
        }
        members.add(student);
    }

    /** Removes a student from members; throws BusinessRuleException for the leader or a non-member. */
    public void removeMember(Student student) {
        if (leader != null && leader.getId().equals(student.getId())) {
            throw new BusinessRuleException("The group leader can't be removed");
        }
        if (!hasMember(student)) {
            throw new BusinessRuleException(student.getName() + " is not in this group");
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(student.getId())) {
                members.remove(i);
                return;
            }
        }
    }

    /** Returns the maximum number of members. */
    public int getMaxSize() {
        return maxSize;
    }

    /** Changes the maximum number of members. */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /** Returns whether the group is publicly discoverable or private. */
    public Visibility getVisibility() {
        return visibility;
    }

    /** Changes the group visibility. */
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /** Returns the group's study goals in declaration order, as a read-only view. */
    public Set<StudyGoal> getGoals() {
        return Collections.unmodifiableSet(goals);
    }

    /** Replaces the group's study goals with a copy of the given set. */
    public void setGoals(Set<StudyGoal> goals) {
        this.goals = EnumSet.noneOf(StudyGoal.class);
        this.goals.addAll(goals);
    }

    /** Returns the group's study mode. */
    public StudyMode getMode() {
        return mode;
    }

    /** Changes the group's study mode. */
    public void setMode(StudyMode mode) {
        this.mode = mode;
    }

    /** Returns true while the group accepts new members; closed groups accept nobody. */
    public boolean isOpen() {
        return open;
    }

    /** Opens or closes the group to new members. */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /** Returns the group's weekly availability grid. */
    public AvailabilityGrid getAvailability() {
        return availability;
    }

    /** Replaces the group's weekly availability grid. */
    public void setAvailability(AvailabilityGrid availability) {
        this.availability = availability;
    }

    /** Returns a short description of this group for logs and debugging. */
    @Override
    public String toString() {
        return "StudyGroup[" + id + " " + name + "]";
    }

    /** Two groups are equal when they have the same id. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StudyGroup)) {
            return false;
        }
        StudyGroup that = (StudyGroup) other;
        return Objects.equals(id, that.id);
    }

    /** Returns a hash code from the id, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
