package com.studybuddy.engine.simulation;

/** How a number moved from the saved config to the proposed one, counting only differences that are not luck. */
public enum Change {

    HIGHER("Higher"),
    LOWER("Lower"),
    ABOUT_THE_SAME("About the same");

    private final String label;

    Change(String label) {
        this.label = label;
    }

    /** Returns the text shown to admins. */
    public String getLabel() {
        return label;
    }

    /**
     * Compares two results by their confidence intervals.
     *
     * @param current the saved config's result
     * @param proposed the proposed config's result
     * @return HIGHER or LOWER only if the intervals do not overlap, otherwise ABOUT_THE_SAME
     */
    public static Change between(Statistic current, Statistic proposed) {
        if (proposed.isClearlyAbove(current)) {
            return HIGHER;
        }
        if (current.isClearlyAbove(proposed)) {
            return LOWER;
        }
        return ABOUT_THE_SAME;
    }
}
