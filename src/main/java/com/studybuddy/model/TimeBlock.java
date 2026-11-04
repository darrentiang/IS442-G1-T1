package com.studybuddy.model;

/** A run of whole hours on one day of the week, e.g. Wednesday 19:00 to 21:00. */
public class TimeBlock {

    private final int day;
    private final int startHour;
    private final int endHour;

    /**
     * Creates a block of time.
     *
     * @param day the day, 0 = Monday
     * @param startHour the first clock hour, e.g. 19
     * @param endHour the clock hour the block ends at (exclusive), e.g. 21
     */
    public TimeBlock(int day, int startHour, int endHour) {
        this.day = day;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    /** Returns the day, 0 = Monday. */
    public int getDay() {
        return day;
    }

    /** Returns the first clock hour of the block. */
    public int getStartHour() {
        return startHour;
    }

    /** Returns the clock hour the block ends at (exclusive). */
    public int getEndHour() {
        return endHour;
    }

    /** Returns the number of hours in the block. */
    public int getLength() {
        return endHour - startHour;
    }

    /** Returns the block as text, e.g. "WED 19-21". */
    @Override
    public String toString() {
        return AvailabilityGrid.dayName(day) + " " + startHour + "-" + endHour;
    }
}
