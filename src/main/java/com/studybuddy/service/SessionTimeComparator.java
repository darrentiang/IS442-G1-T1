package com.studybuddy.service;

import com.studybuddy.model.StudySession;

import java.util.Comparator;

/** Orders study sessions through the week: by day, then start time, then id so the order is stable. */
public class SessionTimeComparator implements Comparator<StudySession> {

    /** Compares two sessions by day, then start hour, then id. */
    @Override
    public int compare(StudySession first, StudySession second) {
        if (first.getDay() != second.getDay()) {
            return Integer.compare(first.getDay(), second.getDay());
        }
        if (first.getStartHour() != second.getStartHour()) {
            return Integer.compare(first.getStartHour(), second.getStartHour());
        }
        return first.getId().compareTo(second.getId());
    }
}
