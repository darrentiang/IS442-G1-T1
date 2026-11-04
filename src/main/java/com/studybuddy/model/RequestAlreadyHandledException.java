package com.studybuddy.model;

/**
 * Thrown when someone answers a join or buddy request that was already accepted or declined.
 */
public class RequestAlreadyHandledException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a user-readable message. */
    public RequestAlreadyHandledException(String message) {
        super(message);
    }
}
