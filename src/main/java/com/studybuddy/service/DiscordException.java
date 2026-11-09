package com.studybuddy.service;

/** Thrown when a call to Discord fails: Discord is unreachable, or it refused the request. */
public class DiscordException extends RuntimeException {

    /** Creates the exception with a message describing what failed. */
    public DiscordException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the error that caused it. */
    public DiscordException(String message, Throwable cause) {
        super(message, cause);
    }
}
