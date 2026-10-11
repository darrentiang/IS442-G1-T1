package com.studybuddy.seed;

/** Thrown when a seed CSV file is missing or has a bad row; stops the app from starting. */
public class SeedDataException extends Exception {

    /** Creates the exception with a message naming the file and line, e.g. "students.csv line 14: ...". */
    public SeedDataException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the lower-level problem that caused it. */
    public SeedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
