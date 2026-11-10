package com.studybuddy.engine.advisor;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.StudentProfile;

/**
 * Marks one block of hours as free in the student's week. Worded "if you're free", because an
 * empty slot in the grid might be a class: the student decides.
 */
public class AddTimeSlotChange extends PreferenceChange {

    private final int day;
    private final int startIndex;
    private final int hours;

    /**
     * Creates the change.
     *
     * @param day 0 for Monday to 6 for Sunday
     * @param startIndex the first hour's index in the grid (0 is 08:00)
     * @param hours how many hours the block lasts
     */
    public AddTimeSlotChange(int day, int startIndex, int hours) {
        this.day = day;
        this.startIndex = startIndex;
        this.hours = hours;
    }

    /** Returns e.g. "slot:3:11:2" (Thursday, from 19:00, for 2 hours). */
    @Override
    public String getKey() {
        return "slot:" + day + ":" + startIndex + ":" + hours;
    }

    /** Applies if the block fits in the grid and is not already all free. */
    @Override
    public boolean appliesTo(StudentProfile profile) {
        if (profile.getAvailability() == null || startIndex < 0
                || startIndex + hours > AvailabilityGrid.HOURS_PER_DAY) {
            return false;
        }
        for (int i = startIndex; i < startIndex + hours; i++) {
            if (!profile.getAvailability().isAvailable(day, i)) {
                return true;
            }
        }
        return false;
    }

    /** Marks every hour of the block as free. */
    @Override
    public void applyTo(StudentProfile profile) {
        for (int i = startIndex; i < startIndex + hours; i++) {
            profile.getAvailability().set(day, i, true);
        }
    }

    /** Returns e.g. "If you're free Thu 19:00–21:00, add it to your free time". */
    @Override
    public String describe() {
        return "If you're free " + blockText() + ", add it to your free time";
    }

    /** Returns e.g. "Added Thu 19:00–21:00 to your free time". */
    @Override
    public String getDoneMessage() {
        return "Added " + blockText() + " to your free time";
    }

    /** Returns a reminder to add it only if the student really is free. */
    @Override
    public String getTradeOff() {
        return "Only add it if you're really free then; buddies will expect you.";
    }

    /** Returns "free time". */
    @Override
    public String getBlockerLabel() {
        return "free time";
    }

    /** Returns the block as text, e.g. "Thu 19:00–21:00". */
    private String blockText() {
        String name = AvailabilityGrid.dayName(day);
        String shortDay = name.charAt(0) + name.substring(1).toLowerCase();
        int startHour = AvailabilityGrid.FIRST_HOUR + startIndex;
        return shortDay + " " + startHour + ":00–" + (startHour + hours) + ":00";
    }
}
