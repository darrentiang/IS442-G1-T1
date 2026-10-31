package com.studybuddy.service;

import com.studybuddy.model.AvailabilityGrid;

/** Turns an AvailabilityGrid back into text such as "MON 19-21;WED 19-21", the format AvailabilityGrid.parse reads. */
public final class AvailabilityFormatter {

    private static final String[] DAY_NAMES = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private static final int DAYS = 7;
    private static final int HOURS = 15;
    private static final int FIRST_HOUR = 8;

    private AvailabilityFormatter() {
    }

    /** Returns the grid as text, merging back-to-back free hours into one range; empty text for an empty grid. */
    public static String format(AvailabilityGrid grid) {
        StringBuilder text = new StringBuilder();
        for (int day = 0; day < DAYS; day++) {
            int hour = 0;
            while (hour < HOURS) {
                if (!grid.isAvailable(day, hour)) {
                    hour++;
                    continue;
                }
                int start = hour;
                while (hour < HOURS && grid.isAvailable(day, hour)) {
                    hour++;
                }
                if (text.length() > 0) {
                    text.append(";");
                }
                text.append(DAY_NAMES[day]).append(" ")
                        .append(start + FIRST_HOUR).append("-").append(hour + FIRST_HOUR);
            }
        }
        return text.toString();
    }
}
