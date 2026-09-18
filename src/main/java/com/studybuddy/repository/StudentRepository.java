package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.Student;

/** Stores and finds students. The rest of the app only ever sees this interface. */
public interface StudentRepository {

    /** Saves a student, replacing any existing student with the same id. */
    void save(Student student);

    /** Returns the student with this id, or null if no such student. */
    Student findById(String id);

    /** Returns all students. */
    List<Student> findAll();
}
