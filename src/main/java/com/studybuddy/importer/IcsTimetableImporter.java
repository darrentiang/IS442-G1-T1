package com.studybuddy.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.studybuddy.model.AvailabilityGrid;

/**
 * Reads an .ics calendar export (RFC 5545), such as the one a university timetable system
 * produces, into the weekly hours the student is in class. java.time is used here, and only
 * here, because calendar files carry real dates and time zones; the result is still a plain
 * boolean grid.
 */
public class IcsTimetableImporter implements TimetableImporter {

    private static final String NAME = "ics";
    private static final String EXTENSION = ".ics";

    private static final String BEGIN_EVENT = "BEGIN:VEVENT";
    private static final String END_EVENT = "END:VEVENT";
    private static final String SUMMARY = "SUMMARY";
    private static final String DTSTART = "DTSTART";
    private static final String DTEND = "DTEND";
    private static final String DATE_ONLY_PARAMETER = "VALUE=DATE";
    private static final char VALUE_SEPARATOR = ':';
    private static final String PARAMETER_SEPARATOR = ";";
    private static final String UTC_SUFFIX = "Z";
    private static final String UNTITLED = "Untitled class";

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Asia/Singapore");

    private static final String[] DAY_NAMES = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private static final int FIRST_HOUR = 8;
    private static final int LAST_HOUR = 23;
    private static final int HOURS_IN_DAY = 24;
    private static final String GRID_RANGE = "08:00-23:00";

    private static final String NO_EVENTS_MESSAGE = "No classes found in this file. Is it an .ics calendar export?";

    /** Returns "ics". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns true for file names ending in .ics, ignoring case. */
    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(EXTENSION);
    }

    /**
     * Reads every VEVENT and marks the hours it covers as busy. Weekly repeats of the same
     * class (same weekday and time on different dates) count as one slot.
     *
     * @param input the uploaded .ics file
     * @return the busy grid, class names, number of weekly slots and warnings
     * @throws TimetableImportException if the file has no events or a date cannot be read
     */
    @Override
    public TimetableImport read(InputStream input) throws TimetableImportException {
        List<String> lines = new ArrayList<>();
        List<Integer> lineNumbers = new ArrayList<>();
        readUnfolded(input, lines, lineNumbers);

        AvailabilityGrid busy = new AvailabilityGrid();
        List<String> classNames = new ArrayList<>();
        List<String> slotKeys = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        boolean foundEvent = false;
        boolean inEvent = false;
        String summary = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        boolean allDay = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().equalsIgnoreCase(BEGIN_EVENT)) {
                foundEvent = true;
                inEvent = true;
                summary = null;
                start = null;
                end = null;
                allDay = false;
                continue;
            }
            if (!inEvent) {
                continue;
            }
            if (line.trim().equalsIgnoreCase(END_EVENT)) {
                inEvent = false;
                String name = (summary == null || summary.isBlank()) ? UNTITLED : summary;
                if (allDay) {
                    addOnce(warnings, "Skipped all-day event \"" + name + "\"");
                } else if (start == null || end == null) {
                    addOnce(warnings, "Skipped \"" + name + "\": it has no start or end time");
                } else {
                    recordClass(name, start, end, busy, classNames, slotKeys, warnings);
                }
                continue;
            }

            int colon = line.indexOf(VALUE_SEPARATOR);
            if (colon < 0) {
                continue;
            }
            String[] nameAndParameters = line.substring(0, colon).split(PARAMETER_SEPARATOR);
            String propertyName = nameAndParameters[0].trim();
            String value = line.substring(colon + 1).trim();
            if (propertyName.equalsIgnoreCase(SUMMARY)) {
                summary = unescape(value);
            } else if (propertyName.equalsIgnoreCase(DTSTART) || propertyName.equalsIgnoreCase(DTEND)) {
                if (isDateOnly(nameAndParameters, value)) {
                    allDay = true;
                } else if (propertyName.equalsIgnoreCase(DTSTART)) {
                    start = parseDateTime(value, lineNumbers.get(i));
                } else {
                    end = parseDateTime(value, lineNumbers.get(i));
                }
            }
        }

        if (!foundEvent) {
            throw new TimetableImportException(NO_EVENTS_MESSAGE);
        }
        return new TimetableImport(busy, classNames, slotKeys.size(), warnings);
    }

    /**
     * Reads the file into logical lines. RFC 5545 "folds" long lines: a line that starts with
     * a space or a tab is the continuation of the line before it, with that one leading
     * character removed. Skipping this step is the classic .ics bug: a long SUMMARY gets cut
     * in half and the second half looks like a broken property. Each logical line keeps the
     * file line number it started on, for error messages.
     */
    private void readUnfolded(InputStream input, List<String> lines, List<Integer> lineNumbers)
            throws TimetableImportException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String physical = reader.readLine();
            int lineNumber = 1;
            while (physical != null) {
                boolean continuation = !physical.isEmpty()
                        && (physical.charAt(0) == ' ' || physical.charAt(0) == '\t');
                if (continuation && !lines.isEmpty()) {
                    int last = lines.size() - 1;
                    lines.set(last, lines.get(last) + physical.substring(1));
                } else {
                    lines.add(physical);
                    lineNumbers.add(lineNumber);
                }
                physical = reader.readLine();
                lineNumber++;
            }
        } catch (IOException e) {
            throw new TimetableImportException("The uploaded file could not be read", e);
        }
    }

    /**
     * Marks the hours one class touches (start rounded down, end rounded up, clipped to the
     * grid's 08:00-23:00) and records its name and weekly slot. A repeat of a slot already
     * seen marks the same cells again and is not counted twice.
     */
    private void recordClass(String name, LocalDateTime start, LocalDateTime end, AvailabilityGrid busy,
            List<String> classNames, List<String> slotKeys, List<String> warnings) {
        int day = start.getDayOfWeek().getValue() - 1;     // DayOfWeek: MONDAY = 1
        String when = DAY_NAMES[day] + " " + start.format(CLOCK_FORMAT) + "-" + end.format(CLOCK_FORMAT);
        if (!end.isAfter(start)) {
            addOnce(warnings, "Skipped \"" + name + "\" on " + when + ": it ends before it starts");
            return;
        }

        int startHour = start.getHour();
        int endHour = end.getHour();
        if (end.getMinute() > 0 || end.getSecond() > 0) {
            endHour++;                                      // round the end up to a whole hour
        }
        if (end.toLocalDate().isAfter(start.toLocalDate())) {
            endHour = HOURS_IN_DAY;                         // runs past midnight: the rest of the day
        }
        int from = Math.max(startHour, FIRST_HOUR);
        int to = Math.min(endHour, LAST_HOUR);
        if (from >= to) {
            addOnce(warnings, "Skipped \"" + name + "\" on " + when + ": it is outside " + GRID_RANGE);
            return;
        }
        if (from > startHour || to < endHour) {
            addOnce(warnings, "\"" + name + "\" on " + when + " runs outside " + GRID_RANGE
                    + "; only the hours inside were marked");
        }

        for (int hour = from; hour < to; hour++) {
            busy.set(day, hour - FIRST_HOUR, true);
        }
        addOnce(classNames, name);
        addOnce(slotKeys, when);
    }

    /** Returns true for an all-day value: VALUE=DATE among the parameters, or a value with no time part. */
    private boolean isDateOnly(String[] nameAndParameters, String value) {
        for (int i = 1; i < nameAndParameters.length; i++) {
            if (nameAndParameters[i].trim().equalsIgnoreCase(DATE_ONLY_PARAMETER)) {
                return true;
            }
        }
        return value.indexOf('T') < 0;
    }

    /**
     * Reads a date-time such as 20260826T153000. A trailing Z means UTC and is converted to
     * Singapore time; a TZID parameter or no zone at all is taken as Singapore time already.
     */
    private LocalDateTime parseDateTime(String value, int lineNumber) throws TimetableImportException {
        try {
            if (value.toUpperCase().endsWith(UTC_SUFFIX)) {
                String withoutSuffix = value.substring(0, value.length() - UTC_SUFFIX.length());
                LocalDateTime utcTime = LocalDateTime.parse(withoutSuffix, DATE_TIME_FORMAT);
                return utcTime.atZone(UTC).withZoneSameInstant(LOCAL_ZONE).toLocalDateTime();
            }
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new TimetableImportException("Line " + lineNumber + ": could not read the date \"" + value + "\"", e);
        }
    }

    /** Undoes the .ics text escapes for commas and semicolons. */
    private String unescape(String text) {
        return text.replace("\\,", ",").replace("\\;", ";").trim();
    }

    /** Adds the text to the list unless it is already there, keeping first-seen order. */
    private void addOnce(List<String> list, String text) {
        if (!list.contains(text)) {
            list.add(text);
        }
    }
}
