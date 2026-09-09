package com.studybuddy.model;

import java.util.List;

/**
 * A read-only snapshot of a profile for display. The only way to read a contact
 * number: it is non-null only when built by StudentProfile.toConnectedView().
 */
public class ProfileView {

    private final String school;
    private final String programme;
    private final int yearOfStudy;
    private final String contactNumber;
    private final List<Course> coursesTaken;
    private final List<Course> completedCourses;
    private final StudyPreferences preferences;
    private final AvailabilityGrid availability;
    private final String lookingFor;

    /** Creates a view; contactNumber is null for the public view. */
    public ProfileView(String school, String programme, int yearOfStudy, String contactNumber,
            List<Course> coursesTaken, List<Course> completedCourses,
            StudyPreferences preferences, AvailabilityGrid availability, String lookingFor) {
        this.school = school;
        this.programme = programme;
        this.yearOfStudy = yearOfStudy;
        this.contactNumber = contactNumber;
        this.coursesTaken = coursesTaken;
        this.completedCourses = completedCourses;
        this.preferences = preferences;
        this.availability = availability;
        this.lookingFor = lookingFor;
    }

    /** Returns the school. */
    public String getSchool() {
        return school;
    }

    /** Returns the degree programme. */
    public String getProgramme() {
        return programme;
    }

    /** Returns the year of study. */
    public int getYearOfStudy() {
        return yearOfStudy;
    }

    /** Returns the contact number, or null when viewing as a stranger. */
    public String getContactNumber() {
        return contactNumber;
    }

    /** Returns the current courses. */
    public List<Course> getCoursesTaken() {
        return coursesTaken;
    }

    /** Returns the completed courses. */
    public List<Course> getCompletedCourses() {
        return completedCourses;
    }

    /** Returns the study preferences. */
    public StudyPreferences getPreferences() {
        return preferences;
    }

    /** Returns the weekly availability grid. */
    public AvailabilityGrid getAvailability() {
        return availability;
    }

    /** Returns the free text shown on match cards. */
    public String getLookingFor() {
        return lookingFor;
    }
}
