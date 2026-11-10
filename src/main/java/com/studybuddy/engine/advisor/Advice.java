package com.studybuddy.engine.advisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BOSS Advisor's answer for one student: whether they need help, how many good matches they have,
 * and either the suggestions that would help or the honest reason none would.
 */
public class Advice {

    private final boolean needsHelp;
    private final int goodMatches;
    private final List<Suggestion> suggestions;
    private final int othersInCourse;
    private final String courseCode;

    /** Creates the advice; use the factory methods below. */
    private Advice(boolean needsHelp, int goodMatches, List<Suggestion> suggestions, int othersInCourse,
            String courseCode) {
        this.needsHelp = needsHelp;
        this.goodMatches = goodMatches;
        this.suggestions = new ArrayList<>(suggestions);
        this.othersInCourse = othersInCourse;
        this.courseCode = courseCode;
    }

    /** Returns advice for a student who already has enough good matches. */
    public static Advice notNeeded(int goodMatches) {
        return new Advice(false, goodMatches, new ArrayList<>(), 0, null);
    }

    /** Returns advice with verified suggestions, best first. */
    public static Advice withSuggestions(int goodMatches, List<Suggestion> suggestions, String courseCode) {
        return new Advice(true, goodMatches, suggestions, 0, courseCode);
    }

    /** Returns advice for a student no single change would help, with how many others need their course. */
    public static Advice nobodyFits(int goodMatches, int othersInCourse, String courseCode) {
        return new Advice(true, goodMatches, new ArrayList<>(), othersInCourse, courseCode);
    }

    /** Returns true if the student has too few good matches. */
    public boolean isNeedsHelp() {
        return needsHelp;
    }

    /** Returns how many good matches the student has now. */
    public int getGoodMatches() {
        return goodMatches;
    }

    /** Returns the suggestions, best first, as a read-only view; empty when none would help. */
    public List<Suggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    /** Returns true if the student needs help but no single change would give them more good matches. */
    public boolean isNobodyFits() {
        return needsHelp && suggestions.isEmpty();
    }

    /** Returns how many other active students need the same target course. */
    public int getOthersInCourse() {
        return othersInCourse;
    }

    /** Returns the student's target course code. */
    public String getCourseCode() {
        return courseCode;
    }
}
