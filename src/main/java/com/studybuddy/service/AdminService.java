package com.studybuddy.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Role;
import com.studybuddy.model.Student;
import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.model.User;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.SystemAdministratorRepository;

/** Admin workflows for user accounts: create, update, delete and status changes. */
@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final SystemAdministratorRepository adminRepository;
    private final UserService userService;

    /** Creates the service with the repositories it writes to and the lookup service (constructor injection). */
    public AdminService(StudentRepository studentRepository, SystemAdministratorRepository adminRepository,
            UserService userService) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.userService = userService;
    }

    /**
     * Creates a student or administrator account from form values, assigning the next free id.
     * Returns the field errors found, or an empty map when the account was created.
     */
    public Map<String, String> createAccount(String role, String name, String email) {
        Map<String, String> fieldErrors = new HashMap<>();
        if (isBlank(role) || (!Role.STUDENT.name().equals(role) && !Role.ADMIN.name().equals(role))) {
            fieldErrors.put("role", "Choose an account type");
        }
        if (isBlank(name)) {
            fieldErrors.put("name", "Name is required");
        }
        if (isBlank(email)) {
            fieldErrors.put("email", "Email is required");
        } else if (!email.contains("@")) {
            fieldErrors.put("email", "Email must contain @");
        } else if (emailExists(email.trim(), null)) {
            fieldErrors.put("email", "This email is already used by another account");
        }
        if (!fieldErrors.isEmpty()) {
            return fieldErrors;
        }
        if (Role.STUDENT.name().equals(role)) {
            studentRepository.save(new Student(nextStudentId(), name.trim(), email.trim()));
        } else {
            adminRepository.save(new SystemAdministrator(nextAdminId(), name.trim(), email.trim()));
        }
        return fieldErrors;
    }

    /**
     * Changes the name and email of the account with this id.
     * Returns the field errors found, or an empty map when the account was updated.
     */
    public Map<String, String> updateAccount(String id, String name, String email) {
        Map<String, String> fieldErrors = new HashMap<>();
        if (isBlank(name)) {
            fieldErrors.put("name", "Name is required");
        }
        if (isBlank(email)) {
            fieldErrors.put("email", "Email is required");
        } else if (!email.contains("@")) {
            fieldErrors.put("email", "Email must contain @");
        } else if (emailExists(email.trim(), id)) {
            fieldErrors.put("email", "This email is already used by another account");
        }
        if (!fieldErrors.isEmpty()) {
            return fieldErrors;
        }
        User user = userService.findUser(id);
        user.setName(name.trim());
        user.setEmail(email.trim());
        if (user.getRole() == Role.STUDENT) {
            studentRepository.save((Student) user);
        } else {
            adminRepository.save((SystemAdministrator) user);
        }
        return fieldErrors;
    }

    /**
     * Deletes the account with this id, unless it is the caller's own account
     * or the last remaining administrator. Returns an error message when nothing
     * was deleted, or null when the account was deleted.
     */
    public String deleteAccount(String id, String currentUserId) {
        if (id.equals(currentUserId)) {
            return "You cannot delete your own account";
        }
        User user = userService.findUser(id);
        if (user == null) {
            return "This account no longer exists";
        }
        if (user.getRole() == Role.ADMIN && adminRepository.findAll().size() == 1) {
            return "The last administrator cannot be deleted";
        }
        if (user.getRole() == Role.STUDENT) {
            studentRepository.deleteById(id);
        } else {
            adminRepository.deleteById(id);
        }
        return null;
    }

    /**
     * Flips the account status: ACTIVE becomes SUSPENDED and back.
     * Returns an error message when the change is not allowed, or null when it happened.
     */
    public String changeStatus(String id, String currentUserId) {
        if (id.equals(currentUserId)) {
            return "You cannot change the status of your own account";
        }
        User user = userService.findUser(id);
        if (user == null) {
            return "This account no longer exists";
        }
        if (user.getStatus() == AccountStatus.ACTIVE) {
            if (user.getRole() == Role.ADMIN && adminRepository.findAll().size() == 1) {
                return "The last administrator cannot be suspended";
            }
            user.setStatus(AccountStatus.SUSPENDED);
        } else {
            user.setStatus(AccountStatus.ACTIVE);
        }
        if (user.getRole() == Role.STUDENT) {
            studentRepository.save((Student) user);
        } else {
            adminRepository.save((SystemAdministrator) user);
        }
        return null;
    }

    /** Returns whether the value is null, empty or whitespace. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Returns whether any account other than the one with excludeId already uses this email. */
    private boolean emailExists(String email, String excludeId) {
        for (Student student : studentRepository.findAll()) {
            if (!student.getId().equals(excludeId) && student.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        for (SystemAdministrator admin : adminRepository.findAll()) {
            if (!admin.getId().equals(excludeId) && admin.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    /** Returns an unused student id, one past the highest existing S number. */
    private String nextStudentId() {
        int highest = 0;
        for (Student student : studentRepository.findAll()) {
            highest = Math.max(highest, numberSuffix(student.getId()));
        }
        return String.format("S%03d", highest + 1);
    }

    /** Returns an unused administrator id, one past the highest existing A number. */
    private String nextAdminId() {
        int highest = 0;
        for (SystemAdministrator admin : adminRepository.findAll()) {
            highest = Math.max(highest, numberSuffix(admin.getId()));
        }
        return String.format("A%03d", highest + 1);
    }

    /** Returns the digits after the leading letter of an id, or 0 if there are none. */
    private int numberSuffix(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
