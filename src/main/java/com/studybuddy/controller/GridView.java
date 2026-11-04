package com.studybuddy.controller;

import com.studybuddy.model.AvailabilityGrid;

/**
 * Presentation helper for a 7-day availability grid on a page. The row and column labels are
 * the same for every grid, so they are inherited from here; each subclass decides how a cell
 * looks by implementing cellClass.
 */
public abstract class GridView {

    /** Returns the CSS class for one cell of the grid. */
    public abstract String cellClass(int day, int hourIndex);

    /** Returns the row labels, MON to SUN. */
    public String[] getDayNames() {
        String[] names = new String[AvailabilityGrid.DAYS];
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            names[day] = AvailabilityGrid.dayName(day);
        }
        return names;
    }

    /** Returns the column labels, "08" to "22" (each column is the hour starting then). */
    public String[] getHourLabels() {
        String[] labels = new String[AvailabilityGrid.HOURS_PER_DAY];
        for (int i = 0; i < AvailabilityGrid.HOURS_PER_DAY; i++) {
            labels[i] = String.format("%02d", AvailabilityGrid.FIRST_HOUR + i);
        }
        return labels;
    }
}
