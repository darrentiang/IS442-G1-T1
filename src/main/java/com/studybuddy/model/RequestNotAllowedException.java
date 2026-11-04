package com.studybuddy.model;

/**
 * Thrown when the rules refuse a buddy request, e.g. a request is already pending.
 */
public class RequestNotAllowedException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a user-readable message. */
    public RequestNotAllowedException(String message) {
        super(message);
    }
}
