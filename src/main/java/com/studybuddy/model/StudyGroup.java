package com.studybuddy.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private Set<StudyGoal> goals = new HashSet<>();
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

    /** Returns the group members. */
    public List<Student> getMembers() {
        return members;
    }

    /** Replaces the group members. */
    public void setMembers(List<Student> members) {
        this.members = members;
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

    /** Returns the group's study goals. */
    public Set<StudyGoal> getGoals() {
        return goals;
    }

    /** Replaces the group's study goals. */
    public void setGoals(Set<StudyGoal> goals) {
        this.goals = goals;
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
}
