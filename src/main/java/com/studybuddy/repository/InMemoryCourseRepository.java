package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Course;

/** LinkedHashMap-backed implementation; keeps insertion order, so findAll() returns rows in CSV order. */
@Repository
public class InMemoryCourseRepository implements CourseRepository {

    private final Map<String, Course> store = new LinkedHashMap<>();

    /** Saves a course, replacing any existing course with the same code. */
    @Override
    public void save(Course course) {
        store.put(course.getCode(), course);
    }

    /** Returns the course with this code, or null if no such course. */
    @Override
    public Course findByCode(String code) {
        return store.get(code);         // the map returns null if the key is absent
    }

    /** Returns all courses, in the order they were saved. */
    @Override
    public List<Course> findAll() {
        return new ArrayList<>(store.values());
    }
}
