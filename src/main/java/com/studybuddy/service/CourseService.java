package com.studybuddy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Course;
import com.studybuddy.repository.CourseRepository;

/** Business rules around courses. Thin for now; grows if rules appear. */
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    /** Creates the service with the repository it reads from (constructor injection). */
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /** Returns all courses. */
    public List<Course> findAll() {
        return courseRepository.findAll();
    }
}
