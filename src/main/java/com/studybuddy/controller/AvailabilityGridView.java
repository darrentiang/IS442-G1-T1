package com.studybuddy.controller;

import com.studybuddy.model.AvailabilityGrid;

/**
 * Presentation helper for a student's own 7-day grid, read-only on My profile and as
 * checkboxes on the availability editor. After a timetable import it also knows which
 * cells are class hours, so the page can mark them "class" and disable them.
 */
public class AvailabilityGridView extends GridView {

    private final AvailabilityGrid availability;
    private final AvailabilityGrid classes;

    /** Creates the view; classes is the imported class grid, or null when nothing was imported. */
    public AvailabilityGridView(AvailabilityGrid availability, AvailabilityGrid classes) {
        this.availability = availability;
        this.classes = classes;
    }

    /** Returns true if the student is marked free in this cell (a null grid is never free). */
    public boolean isFree(int day, int hourIndex) {
        return availability != null && availability.isAvailable(day, hourIndex);
    }

    /** Returns true if the imported timetable has a class in this cell. */
    public boolean isClass(int day, int hourIndex) {
        return classes != null && classes.isAvailable(day, hourIndex);
    }

    /** Returns "cell-free" or "cell-busy" for one cell of the read-only grid. */
    @Override
    public String cellClass(int day, int hourIndex) {
        return isFree(day, hourIndex) ? "cell-free" : "cell-busy";
    }

    /** Returns the checkbox value for one cell, "day-hourIndex", e.g. "2-11". */
    public String cellValue(int day, int hourIndex) {
        return day + "-" + hourIndex;
    }
}
