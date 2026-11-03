package com.studybuddy.engine;

/** One criterion's verdict on a pair: a score from 0.0 to 1.0 and an optional reason to show. */
public class CriterionScore {

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_VALUE = 1.0;

    private final double value;
    private final String reason;

    /**
     * Creates a score.
     *
     * @param value the score, from 0.0 (no fit) to 1.0 (perfect fit)
     * @param reason a short sentence for the match card, or null when there is nothing worth saying
     * @throws IllegalArgumentException if value is outside 0.0 to 1.0
     */
    public CriterionScore(double value, String reason) {
        if (!(value >= MIN_VALUE && value <= MAX_VALUE)) {
            throw new IllegalArgumentException("score must be between 0.0 and 1.0 but was " + value);
        }
        this.value = value;
        this.reason = reason;
    }

    /** Returns the score, from 0.0 to 1.0. */
    public double getValue() {
        return value;
    }

    /** Returns the reason to show, or null when there is nothing worth saying. */
    public String getReason() {
        return reason;
    }
}
