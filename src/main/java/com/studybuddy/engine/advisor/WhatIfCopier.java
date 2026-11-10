package com.studybuddy.engine.advisor;

import java.util.ArrayList;
import java.util.EnumSet;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyPreferences;

/**
 * Copies a student so BOSS Advisor can try a change without touching the real one. The copy keeps
 * the same id, so the search still leaves out the student themselves, their buddies, pending
 * requests and people they passed on. The contact number is not copied: matching never uses it.
 */
public class WhatIfCopier {

    /** Returns a deep copy of the student's profile, preferences and availability, on a new Student. */
    public Student copy(Student original) {
        Student copy = new Student(original.getId(), original.getName(), original.getEmail());
        copy.setStatus(original.getStatus());
        StudentProfile profile = original.getProfile();
        if (profile == null) {
            return copy;
        }
        StudentProfile profileCopy = new StudentProfile();
        profileCopy.setSchool(profile.getSchool());
        profileCopy.setProgramme(profile.getProgramme());
        profileCopy.setYearOfStudy(profile.getYearOfStudy());
        profileCopy.setCoursesTaken(new ArrayList<>(profile.getCoursesTaken()));
        profileCopy.setCompletedCourses(new ArrayList<>(profile.getCompletedCourses()));
        profileCopy.setLookingFor(profile.getLookingFor());
        profileCopy.setPreferences(copyPreferences(profile.getPreferences()));
        profileCopy.setAvailability(copyGrid(profile.getAvailability()));
        copy.setProfile(profileCopy);
        return copy;
    }

    /** Returns a copy of the preferences, or null if there are none. */
    private StudyPreferences copyPreferences(StudyPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        StudyPreferences copy = new StudyPreferences();
        copy.setTargetCourse(preferences.getTargetCourse());
        copy.setMode(preferences.getMode());
        copy.setFormat(preferences.getFormat());
        EnumSet<StudyGoal> goals = EnumSet.noneOf(StudyGoal.class);
        goals.addAll(preferences.getGoals());
        copy.setGoals(goals);
        copy.setIntensity(preferences.getIntensity());
        return copy;
    }

    /** Returns a cell-by-cell copy of the grid, or an empty grid if there is none. */
    private AvailabilityGrid copyGrid(AvailabilityGrid grid) {
        AvailabilityGrid copy = new AvailabilityGrid();
        if (grid == null) {
            return copy;
        }
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            for (int hour = 0; hour < AvailabilityGrid.HOURS_PER_DAY; hour++) {
                copy.set(day, hour, grid.isAvailable(day, hour));
            }
        }
        return copy;
    }
}
