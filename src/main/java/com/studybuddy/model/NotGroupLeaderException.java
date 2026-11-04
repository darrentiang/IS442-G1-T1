package com.studybuddy.model;

/**
 * Thrown when someone other than the group's leader tries an action only the leader may take.
 */
public class NotGroupLeaderException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a user-readable message. */
    public NotGroupLeaderException(String message) {
        super(message);
    }
}
