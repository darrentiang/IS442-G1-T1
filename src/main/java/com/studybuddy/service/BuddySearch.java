package com.studybuddy.service;

/**
 * Raw values from the Find Buddies search bar, as text; MatchingService converts them into
 * the engine's MatchContext. A blank value means "any".
 */
public class BuddySearch {

    private String courseCode;
    private String goal;
    private String mode;
    private int minHours;

    /** Creates an empty search; the controller fills it from the request parameters. */
    public BuddySearch() {
    }

    /** Returns the course code searched for, or blank for any course. */
    public String getCourseCode() {
        return courseCode;
    }

    /** Sets the course code searched for. */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /** Returns the study goal searched for, as a StudyGoal name, or blank for any goal. */
    public String getGoal() {
        return goal;
    }

    /** Sets the study goal searched for, as a StudyGoal name. */
    public void setGoal(String goal) {
        this.goal = goal;
    }

    /** Returns the study mode searched for, as a StudyMode name, or blank for any mode. */
    public String getMode() {
        return mode;
    }

    /** Sets the study mode searched for, as a StudyMode name. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** Returns the fewest usable hours together a candidate must have; 0 means no limit. */
    public int getMinHours() {
        return minHours;
    }

    /** Sets the fewest usable hours together a candidate must have. */
    public void setMinHours(int minHours) {
        this.minHours = minHours;
    }
}
