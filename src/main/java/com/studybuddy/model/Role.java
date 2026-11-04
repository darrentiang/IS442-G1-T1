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

    /** Returns the Role whose name() is exactly this text, or null for null, blank or unknown text. */
    public static Role fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (Role value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
