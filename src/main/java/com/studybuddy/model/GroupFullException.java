package com.studybuddy.model;

/**
 * Thrown when a student is added to a group that already has maxSize members.
 * It is an IllegalStateException, so code that catches IllegalStateException catches it too.
 */
public class GroupFullException extends IllegalStateException {

    /** Creates the exception with a user-readable message. */
    public GroupFullException(String message) {
        super(message);
    }
}
