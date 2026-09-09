package com.studybuddy.model;

import java.util.HashSet;
import java.util.Set;

/** What a student wants in a buddy. */
public class StudyPreferences {

    private Course targetCourse;
    private StudyMode mode;
    private GroupFormat format;
    private Set<StudyGoal> goals = new HashSet<>();
    private Intensity intensity;

    /** Returns the course this student needs a buddy for, or null if not chosen yet. */
    public Course getTargetCourse() {
        return targetCourse;
    }

    /** Changes the course this student needs a buddy for. */
    public void setTargetCourse(Course targetCourse) {
        this.targetCourse = targetCourse;
    }

    /** Returns the preferred study mode, or null if not chosen yet. */
    public StudyMode getMode() {
        return mode;
    }

    /** Changes the preferred study mode. */
    public void setMode(StudyMode mode) {
        this.mode = mode;
    }

    /** Returns the preferred group format, or null if not chosen yet. */
    public GroupFormat getFormat() {
        return format;
    }

    /** Changes the preferred group format. */
    public void setFormat(GroupFormat format) {
        this.format = format;
    }

    /** Returns the set of study goals; empty until chosen. */
    public Set<StudyGoal> getGoals() {
        return goals;
    }

    /** Replaces the set of study goals. */
    public void setGoals(Set<StudyGoal> goals) {
        this.goals = goals;
    }

    /** Returns the intended study intensity, or null if not chosen yet. */
    public Intensity getIntensity() {
        return intensity;
    }

    /** Changes the intended study intensity. */
    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }
}
