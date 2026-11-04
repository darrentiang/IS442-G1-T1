package com.studybuddy.importer;

import java.io.InputStream;

/**
 * Reads one kind of timetable file into the hours a student is in class. One class per file
 * format; TimetableImportService picks the right one by asking each importer supports(fileName).
 * B3b adds ScreenshotTimetableImporter behind this same interface.
 */
public interface TimetableImporter {

    /** Returns a short name for this importer, e.g. "ics". */
    String getName();

    /** Returns true if this importer can read a file with this name (by extension, case-insensitive). */
    boolean supports(String fileName);

    /**
     * Reads the file and returns the weekly class hours it found.
     *
     * @param input the uploaded file's contents
     * @return the busy hours, class names, class count and any warnings
     * @throws TimetableImportException if the file cannot be read as a timetable
     */
    TimetableImport read(InputStream input) throws TimetableImportException;
}
