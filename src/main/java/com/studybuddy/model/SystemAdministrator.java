package com.studybuddy.model;

/** The system administrator. Extends User. */
public class SystemAdministrator extends User {

    /** Creates an administrator account. */
    public SystemAdministrator(String id, String name, String email) {
        super(id, name, email);
    }

    /** Returns Role.ADMIN. */
    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    /** Returns a short description of this administrator for logs and debugging. */
    @Override
    public String toString() {
        return "SystemAdministrator[" + getId() + " " + getName() + "]";
    }
}
