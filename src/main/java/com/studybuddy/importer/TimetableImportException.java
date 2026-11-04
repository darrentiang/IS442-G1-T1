package com.studybuddy.importer;

/** Thrown when an uploaded timetable cannot be read. Checked, so callers must decide what to tell the student. */
public class TimetableImportException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with a message the student can read. */
    public TimetableImportException(String message) {
        super(message);
    }

    /** Creates the exception with a message the student can read and the low-level cause. */
    public TimetableImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
