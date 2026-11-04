package com.studybuddy.model;

import java.util.ArrayList;
import java.util.List;

/** A weekly availability (a student's or a group's): 7 days x 15 hourly cells (08:00-23:00). Day 0 = Monday, hour index 0 = 08:00. */
public class AvailabilityGrid {

    /** Number of days in the grid, Monday to Sunday. */
    public static final int DAYS = 7;
    /** Number of hourly cells per day, 08:00 to 23:00. */
    public static final int HOURS_PER_DAY = 15;
    /** The clock hour of hour index 0. */
    public static final int FIRST_HOUR = 8;
    /** The clock hour the last cell ends at (exclusive). */
    public static final int LAST_HOUR = 23;

    private static final String[] DAY_NAMES = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

    private final boolean[][] cells = new boolean[DAYS][HOURS_PER_DAY];

    /**
     * Builds a grid from text such as "MON 19-21;WED 19-21;SAT 14-18". The end hour is
     * exclusive, so "19-21" marks 19:00 and 20:00. Blank text gives an empty grid.
     *
     * @param text the availability text, in the format above
     * @return the filled grid
     * @throws IllegalArgumentException if a day name or hour range is not valid
     */
    public static AvailabilityGrid parse(String text) {
        AvailabilityGrid grid = new AvailabilityGrid();
        if (text == null || text.isBlank()) {
            return grid;
        }
        String[] slots = text.split(";");
        for (String slot : slots) {
            String trimmed = slot.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(" ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("expected 'DAY start-end' but found '" + trimmed + "'");
            }
            int day = dayIndex(parts[0]);
            String[] hours = parts[1].split("-");
            if (hours.length != 2) {
                throw new IllegalArgumentException("expected an hour range like 19-21 but found '" + parts[1] + "'");
            }
            int start = parseHour(hours[0], trimmed);
            int end = parseHour(hours[1], trimmed);
            if (start < FIRST_HOUR || end > LAST_HOUR) {
                throw new IllegalArgumentException("hours must be within " + FIRST_HOUR + "-" + LAST_HOUR
                        + ", found '" + trimmed + "'");
            }
            if (start >= end) {
                throw new IllegalArgumentException("start hour must be before end hour, found '" + trimmed + "'");
            }
            for (int hour = start; hour < end; hour++) {
                grid.set(day, hour - FIRST_HOUR, true);
            }
        }
        return grid;
    }

    /** Returns the short name of a day, "MON" for 0 up to "SUN" for 6. */
    public static String dayName(int day) {
        return DAY_NAMES[day];
    }

    /** Converts MON..SUN to 0..6, or throws IllegalArgumentException for any other text. */
    public static int dayIndex(String dayName) {
        for (int i = 0; i < DAY_NAMES.length; i++) {
            if (DAY_NAMES[i].equals(dayName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("unknown day '" + dayName + "', expected MON to SUN");
    }

    /** Reads one hour number, or throws if it is not a whole number. */
    private static int parseHour(String hourText, String slot) {
        try {
            return Integer.parseInt(hourText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("hour '" + hourText + "' is not a whole number in '" + slot + "'", e);
        }
    }

    /** Returns true if the student is free on the given day at the given hour index. */
    public boolean isAvailable(int day, int hourIndex) {
        return cells[day][hourIndex];
    }

    /** Marks one cell of the grid as free or busy. */
    public void set(int day, int hourIndex, boolean available) {
        cells[day][hourIndex] = available;
    }

    /** Counts the hours per week where BOTH grids are true. */
    public int overlapHours(AvailabilityGrid other) {
        int count = 0;
        for (int day = 0; day < DAYS; day++) {
            for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
                if (cells[day][hour] && other.cells[day][hour]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Finds every run of consecutive hours where BOTH grids are free on the same day and the
     * run is at least minHours long, in day then hour order.
     *
     * @param other the grid to compare with
     * @param minHours the shortest run worth keeping
     * @return the shared blocks, empty if there are none
     */
    public List<TimeBlock> commonBlocks(AvailabilityGrid other, int minHours) {
        List<TimeBlock> blocks = new ArrayList<>();
        for (int day = 0; day < DAYS; day++) {
            int runStart = -1;
            for (int hour = 0; hour <= HOURS_PER_DAY; hour++) {
                boolean bothFree = hour < HOURS_PER_DAY && cells[day][hour] && other.cells[day][hour];
                if (bothFree && runStart < 0) {
                    runStart = hour;
                } else if (!bothFree && runStart >= 0) {
                    if (hour - runStart >= minHours) {
                        blocks.add(new TimeBlock(day, FIRST_HOUR + runStart, FIRST_HOUR + hour));
                    }
                    runStart = -1;
                }
            }
        }
        return blocks;
    }

    /**
     * Adds up the hours in the shared blocks of at least minHours. Short scattered overlaps
     * are not counted, because nobody can hold a study session in them.
     *
     * @param other the grid to compare with
     * @param minHours the shortest run worth counting
     * @return the total hours in those blocks
     */
    public int usableHours(AvailabilityGrid other, int minHours) {
        int total = 0;
        List<TimeBlock> blocks = commonBlocks(other, minHours);
        for (TimeBlock block : blocks) {
            total += block.getLength();
        }
        return total;
    }
}
