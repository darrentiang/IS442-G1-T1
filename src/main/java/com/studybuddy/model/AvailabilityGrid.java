package com.studybuddy.model;

/** A student's weekly availability: 7 days x 15 hourly cells (08:00-23:00). Day 0 = Monday, hour index 0 = 08:00. */
public class AvailabilityGrid {

    private final boolean[][] cells = new boolean[7][15];

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
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 15; hour++) {
                if (cells[day][hour] && other.cells[day][hour]) {
                    count++;
                }
            }
        }
        return count;
    }
}
