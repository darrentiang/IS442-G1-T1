package com.studybuddy.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.studybuddy.model.Course;
import com.studybuddy.repository.CourseRepository;

/** Fills the in-memory repositories with demo data once, when the app starts. */
@Component
public class SeedDataLoader implements CommandLineRunner {

    private final CourseRepository courseRepository;

    /** Creates the loader with the repositories it fills (constructor injection). */
    public SeedDataLoader(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /** Runs once at startup and saves the seed data. */
    @Override
    public void run(String... args) {
        seedCourses();
    }

    private void seedCourses() {
        courseRepository.save(new Course("IS442", "Object-Oriented Programming", "SCIS"));
        courseRepository.save(new Course("IS212", "Software Project Management", "SCIS"));
        courseRepository.save(new Course("IS216", "Web Application Development II", "SCIS"));
        courseRepository.save(new Course("IS112", "Data Management", "SCIS"));
        courseRepository.save(new Course("IS113", "Web Application Development I", "SCIS"));
        courseRepository.save(new Course("CS301", "IT Solution Architecture", "SCIS"));
        courseRepository.save(new Course("IS215", "Digital Business", "SCIS"));
        courseRepository.save(new Course("STAT101", "Introductory Statistics", "SOSS"));
        courseRepository.save(new Course("IS111", "Intro to Programming", "SCIS"));
        courseRepository.save(new Course("IS210", "Business Process Analysis", "SCIS"));
    }
}
