package com.studybuddy.model;

import java.util.Objects;

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

    /** Two courses are equal when they have the same course code. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Course)) {
            return false;
        }
        Course that = (Course) other;
        return Objects.equals(code, that.code);
    }

    /** Returns a hash code from the code, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
