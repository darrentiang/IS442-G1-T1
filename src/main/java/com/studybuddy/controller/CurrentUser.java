package com.studybuddy.controller;

import com.studybuddy.model.Role;
import com.studybuddy.model.Student;
import com.studybuddy.model.User;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Remembers who is signed in, using the web session. Sessions are a web concern, so this
 * helper lives in the controller layer and services stay web-free. Static methods only;
 * it is a helper, not a Spring bean.
 */
public final class CurrentUser {

    private static final String ID = "currentUserId";
    private static final String NAME = "currentUserName";
    private static final String ROLE = "currentUserRole";

    private CurrentUser() {
    }

    /** Records the user as signed in for this browser session. */
    public static void signIn(HttpSession session, User user) {
        session.setAttribute(ID, user.getId());
        session.setAttribute(NAME, user.getName());
        session.setAttribute(ROLE, user.getRole());
    }

    /** Updates the name shown in the nav after the signed-in user renames themselves. */
    public static void updateName(HttpSession session, String name) {
        session.setAttribute(NAME, name);
    }

    /** Returns the signed-in user's id, or null if nobody is signed in. */
    public static String getId(HttpSession session) {
        return (String) session.getAttribute(ID);
    }

    /** Returns the signed-in user's role (Role.STUDENT or Role.ADMIN), or null if nobody is signed in. */
    public static Role getRole(HttpSession session) {
        return (Role) session.getAttribute(ROLE);
    }

    /** Returns the signed-in user as a Student, or null if nobody is signed in or the user is not a student. */
    public static Student studentOrNull(HttpSession session, UserService userService) {
        String id = getId(session);
        if (id == null) {
            return null;
        }
        return userService.findStudent(id);
    }

    /** Signs the user out by throwing the whole session away. */
    public static void signOut(HttpSession session) {
        session.invalidate();
    }
}
