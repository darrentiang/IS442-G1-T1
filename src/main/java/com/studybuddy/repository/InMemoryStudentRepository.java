package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Student;

/** LinkedHashMap-backed implementation; keeps insertion order, so findAll() returns rows in CSV order. */
@Repository
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<String, Student> store = new LinkedHashMap<>();

    /** Saves a student, replacing any existing student with the same id. */
    @Override
    public void save(Student student) {
        store.put(student.getId(), student);
    }

    /** Returns the student with this id, or null if no such student. */
    @Override
    public Student findById(String id) {
        return store.get(id);
    }

    /** Returns all students, in the order they were saved. */
    @Override
    public List<Student> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Removes the student with this id, if there is one. */
    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
