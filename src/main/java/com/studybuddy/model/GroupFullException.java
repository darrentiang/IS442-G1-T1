package com.studybuddy.model;

/**
 * Thrown when a student is added to, or asks to join, a group that already has maxSize members.
 */
public class GroupFullException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a user-readable message. */
    public GroupFullException(String message) {
        super(message);
    }
}
