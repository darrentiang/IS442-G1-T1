package com.studybuddy.model;

/**
 * Thrown when an action breaks a rule of the system (not a programming error), such as joining
 * a closed group. The message is written for people; controllers catch this type and turn it
 * into an error toast. Subclasses name the rules that some callers want to tell apart.
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a user-readable message. */
    public BusinessRuleException(String message) {
        super(message);
    }
}
