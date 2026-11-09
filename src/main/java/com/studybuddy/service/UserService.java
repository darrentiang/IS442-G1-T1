package com.studybuddy.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Role;
import com.studybuddy.model.Student;
import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.model.User;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.SystemAdministratorRepository;

/** Finds users (students and administrators) and records their sign-ins. Knows nothing about web sessions. */
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

    /** Returns all users (students and administrators), in seed order. */
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        users.addAll(findAllStudents());
        users.addAll(findAllAdmins());
        return users;
    }

    /**
     * Returns the accounts that match every filter given; a blank filter matches everything.
     *
     * @param query text to find in the name, email or id, ignoring case
     * @param role a Role name (STUDENT, ADMIN), or blank for both
     * @param status an AccountStatus name (ACTIVE, SUSPENDED), or blank for both
     * @return the matching accounts, students first, in the order findAllUsers returns them
     */
    public List<User> searchUsers(String query, String role, String status) {
        String text = query == null ? "" : query.trim().toLowerCase();
        List<User> result = new ArrayList<>();
        for (User user : findAllUsers()) {
            boolean textMatches = text.isEmpty()
                    || user.getName().toLowerCase().contains(text)
                    || user.getEmail().toLowerCase().contains(text)
                    || user.getId().toLowerCase().contains(text);
            boolean roleMatches = role == null || role.isBlank() || user.getRole().name().equals(role);
            boolean statusMatches = status == null || status.isBlank() || user.getStatus().name().equals(status);
            if (textMatches && roleMatches && statusMatches) {
                result.add(user);
            }
        }
        return result;
    }

    /** Stamps the current time as this user's last sign-in and saves the user. */
    public void recordSignIn(User user) {
        user.setLastSignedIn(LocalDateTime.now());
        if (user.getRole() == Role.STUDENT) {
            studentRepository.save((Student) user);
        } else {
            adminRepository.save((SystemAdministrator) user);
        }
    }
}