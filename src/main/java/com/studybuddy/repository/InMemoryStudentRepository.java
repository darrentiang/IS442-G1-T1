package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Student;

/** HashMap-backed implementation. */
@Repository
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<String, Student> store = new HashMap<>();

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

    /** Returns all students. */
    @Override
    public List<Student> findAll() {
        return new ArrayList<>(store.values());
    }
}
