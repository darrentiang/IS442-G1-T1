package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.Course;

/** Stores and finds courses. The rest of the app only ever sees this interface. */
public interface CourseRepository {

    /** Saves a course, replacing any existing course with the same code. */
    void save(Course course);

    /** Returns the course with this code, or null if no such course. */
    Course findByCode(String code);

    /** Returns all courses. */
    List<Course> findAll();
}
