package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

/** Raw values from the create/edit group form, all as text; StudyGroupService validates and converts them. */
public class GroupForm {

    private String name;
    private String description;
    private String courseCode;
    private List<String> goals = new ArrayList<>();
    private String mode;
    private String availabilityText;
    private String maxSize;
    private String visibility;

    /** Creates an empty form; the controller fills it from the request parameters. */
    public GroupForm() {
    }

    /** Returns the group name as typed. */
    public String getName() {
        return name;
    }

    /** Sets the group name as typed. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the description as typed. */
    public String getDescription() {
        return description;
    }

    /** Sets the description as typed. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns the chosen course code. */
    public String getCourseCode() {
        return courseCode;
    }

    /** Sets the chosen course code. */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /** Returns the ticked study goals, as StudyGoal names. */
    public List<String> getGoals() {
        return goals;
    }

    /** Sets the ticked study goals, as StudyGoal names. */
    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    /** Returns the chosen study mode, as a StudyMode name. */
    public String getMode() {
        return mode;
    }

    /** Sets the chosen study mode, as a StudyMode name. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** Returns the availability text, such as "MON 19-21;WED 19-21". */
    public String getAvailabilityText() {
        return availabilityText;
    }

    /** Sets the availability text, such as "MON 19-21;WED 19-21". */
    public void setAvailabilityText(String availabilityText) {
        this.availabilityText = availabilityText;
    }

    /** Returns the max size as typed (not yet checked to be a number). */
    public String getMaxSize() {
        return maxSize;
    }

    /** Sets the max size as typed. */
    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    /** Returns the chosen visibility, as a Visibility name. */
    public String getVisibility() {
        return visibility;
    }

    /** Sets the chosen visibility, as a Visibility name. */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
