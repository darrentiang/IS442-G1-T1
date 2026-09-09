package com.studybuddy.model;

/** A course students can be matched on. We seed exactly 10. */
public class Course {

    private final String code;
    private final String title;
    private final String school;

    /** Creates a course, e.g. ("IS442", "Object-Oriented Programming", "SCIS"). */
    public Course(String code, String title, String school) {
        this.code = code;
        this.title = title;
        this.school = school;
    }

    /** Returns the course code, e.g. "IS442". */
    public String getCode() {
        return code;
    }

    /** Returns the course title. */
    public String getTitle() {
        return title;
    }

    /** Returns the school offering the course, e.g. "SCIS". */
    public String getSchool() {
        return school;
    }

    /** Returns a short description of this course for logs and debugging. */
    @Override
    public String toString() {
        return "Course[" + code + " " + title + "]";
    }
}
