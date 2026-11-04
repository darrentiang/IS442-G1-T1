package com.studybuddy.service;

import com.studybuddy.model.AvailabilityGrid;

/** Turns an AvailabilityGrid back into text such as "MON 19-21;WED 19-21", the format AvailabilityGrid.parse reads. */
public final class AvailabilityFormatter {

    private AvailabilityFormatter() {
    }

    /** Returns the grid as text, merging back-to-back free hours into one range; empty text for an empty grid. */
    public static String format(AvailabilityGrid grid) {
        StringBuilder text = new StringBuilder();
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            int hour = 0;
            while (hour < AvailabilityGrid.HOURS_PER_DAY) {
                if (!grid.isAvailable(day, hour)) {
                    hour++;
                    continue;
                }
                int start = hour;
                while (hour < AvailabilityGrid.HOURS_PER_DAY && grid.isAvailable(day, hour)) {
                    hour++;
                }
                if (text.length() > 0) {
                    text.append(";");
                }
                text.append(AvailabilityGrid.dayName(day)).append(" ")
                        .append(start + AvailabilityGrid.FIRST_HOUR).append("-")
                        .append(hour + AvailabilityGrid.FIRST_HOUR);
            }
        }
        return text.toString();
    }
}
