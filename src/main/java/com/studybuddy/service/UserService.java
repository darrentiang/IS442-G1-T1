package com.studybuddy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Student;
import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.model.User;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.SystemAdministratorRepository;

/** Finds users (students and administrators). Knows nothing about web sessions. */
@Service
public class UserService {

    private final StudentRepository studentRepository;
    private final SystemAdministratorRepository adminRepository;

    /** Creates the service with the repositories it reads from (constructor injection). */
    public UserService(StudentRepository studentRepository, SystemAdministratorRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    /** Returns the student with this id, or null if there is none. */
    public Student findStudent(String id) {
        return studentRepository.findById(id);
    }

    /** Returns the user (student or administrator) with this id, or null if there is none. */
    public User findUser(String id) {
        Student student = studentRepository.findById(id);
        if (student != null) {
            return student;
        }
        return adminRepository.findById(id);
    }

    /** Returns all students, in seed order. */
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    /** Returns all administrators, in seed order. */
    public List<SystemAdministrator> findAllAdmins() {
        return adminRepository.findAll();
    }
}
