package com.studybuddy.model;

/** The kind of account a user has. Fixed choices: student or administrator. */
public enum Role {

    STUDENT("Student"),
    ADMIN("Administrator");

    private final String label;

    /** Creates a role with the name shown to people. */
    Role(String label) {
        this.label = label;
    }

    /** Returns the name shown to people, e.g. "Administrator". */
    public String getLabel() {
        return label;
    }
}
