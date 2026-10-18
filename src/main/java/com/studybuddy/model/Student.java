package com.studybuddy.model;

/**
 * A student. Extends User. Leadership of a group is NOT a subclass —
 * a leader is just the Student that a StudyGroup points at.
 */
public class Student extends User {

    private StudentProfile profile;

    /** Creates a student account; the profile is filled in separately. */
    public Student(String id, String name, String email) {
        super(id, name, email);
    }

    /** Returns this student's study-facing profile, or null if not yet created. */
    public StudentProfile getProfile() {
        return profile;
    }

    /** Attaches or replaces this student's profile. */
    public void setProfile(StudentProfile profile) {
        this.profile = profile;
    }

    /** Returns Role.STUDENT. */
    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    /** Returns a short description of this student for logs and debugging. */
    @Override
    public String toString() {
        return "Student[" + getId() + " " + getName() + "]";
    }
}
