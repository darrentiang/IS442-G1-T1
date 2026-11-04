package com.studybuddy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A student's study-facing data. Account identity lives on User; this is what gets matched. */
public class StudentProfile {

    private String school;
    private String programme;
    private int yearOfStudy;
    private String contactNumber;   // PRIVATE. No getter — readable only via toConnectedView().
    private List<Course> coursesTaken = new ArrayList<>();
    private List<Course> completedCourses = new ArrayList<>();
    private StudyPreferences preferences;
    private AvailabilityGrid availability = new AvailabilityGrid();
    private String lookingFor;

    /** Returns the school, e.g. "SCIS". */
    public String getSchool() {
        return school;
    }

    /** Changes the school. */
    public void setSchool(String school) {
        this.school = school;
    }

    /** Returns the degree programme, e.g. "Information Systems". */
    public String getProgramme() {
        return programme;
    }

    /** Changes the degree programme. */
    public void setProgramme(String programme) {
        this.programme = programme;
    }

    /** Returns the year of study. */
    public int getYearOfStudy() {
        return yearOfStudy;
    }

    /** Changes the year of study. */
    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    /** Changes the contact number. There is deliberately no getter for it. */
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /** Returns the current courses (worksheet import fills this), as a read-only view. */
    public List<Course> getCoursesTaken() {
        return Collections.unmodifiableList(coursesTaken);
    }

    /** Returns true if one of the current courses has this course code. */
    public boolean isTaking(String courseCode) {
        for (Course course : coursesTaken) {
            if (course.getCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }

    /** Replaces the current courses with a copy of the given list. */
    public void setCoursesTaken(List<Course> coursesTaken) {
        this.coursesTaken = new ArrayList<>(coursesTaken);
    }

    /** Returns the completed courses (display + discovery panel only, never scored), as a read-only view. */
    public List<Course> getCompletedCourses() {
        return Collections.unmodifiableList(completedCourses);
    }

    /** Replaces the completed courses with a copy of the given list. */
    public void setCompletedCourses(List<Course> completedCourses) {
        this.completedCourses = new ArrayList<>(completedCourses);
    }

    /** Returns what this student wants in a buddy, or null if not set yet. */
    public StudyPreferences getPreferences() {
        return preferences;
    }

    /** Changes what this student wants in a buddy. */
    public void setPreferences(StudyPreferences preferences) {
        this.preferences = preferences;
    }

    /** Returns the weekly availability grid. */
    public AvailabilityGrid getAvailability() {
        return availability;
    }

    /** Replaces the weekly availability grid. */
    public void setAvailability(AvailabilityGrid availability) {
        this.availability = availability;
    }

    /** Returns the free text shown on match cards. */
    public String getLookingFor() {
        return lookingFor;
    }

    /** Changes the free text shown on match cards. */
    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }

    /** Profile as strangers see it: contactNumber is omitted. */
    public ProfileView toPublicView() {
        return new ProfileView(school, programme, yearOfStudy, null,
                coursesTaken, completedCourses, preferences, availability, lookingFor);
    }

    /** Profile as an accepted connection sees it: contactNumber included. */
    public ProfileView toConnectedView() {
        return new ProfileView(school, programme, yearOfStudy, contactNumber,
                coursesTaken, completedCourses, preferences, availability, lookingFor);
    }
}
