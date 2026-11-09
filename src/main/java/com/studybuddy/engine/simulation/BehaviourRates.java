package com.studybuddy.engine.simulation;

/**
 * The behaviour numbers a simulation runs on: how many requests a student sends each round,
 * how often requests are left unanswered, how often answered requests are accepted at low,
 * medium and high match scores, and how much a crowded inbox lowers acceptance.
 */
public class BehaviourRates {

    /** Match scores below this are "low". */
    public static final double MEDIUM_SCORE = 0.50;
    /** Match scores at or above this are "high". */
    public static final double HIGH_SCORE = 0.65;
    /** A student with at least this many requests waiting counts as busy. */
    public static final int BUSY_INBOX = 3;

    private static final int LOW = 0;
    private static final int MEDIUM = 1;
    private static final int HIGH = 2;

    private final Estimate requestsPerRound;
    private final Estimate ignoreRate;
    private final Estimate[] acceptRateByBand;
    private final Estimate busyFactor;

    /**
     * Creates the rates.
     *
     * @param requestsPerRound average requests a searching student sends each round
     * @param ignoreRate share of requests left unanswered
     * @param lowAccept share of answered requests accepted when the match score is low
     * @param mediumAccept the same for medium match scores
     * @param highAccept the same for high match scores
     * @param busyFactor how much a busy inbox multiplies the chance of accepting (1 = no effect)
     */
    public BehaviourRates(Estimate requestsPerRound, Estimate ignoreRate, Estimate lowAccept,
            Estimate mediumAccept, Estimate highAccept, Estimate busyFactor) {
        this.requestsPerRound = requestsPerRound;
        this.ignoreRate = ignoreRate;
        this.acceptRateByBand = new Estimate[] {lowAccept, mediumAccept, highAccept};
        this.busyFactor = busyFactor;
    }

    /** Returns which score band a match score falls in: 0 low, 1 medium, 2 high. */
    public static int bandOf(double mutualScore) {
        if (mutualScore >= HIGH_SCORE) {
            return HIGH;
        }
        if (mutualScore >= MEDIUM_SCORE) {
            return MEDIUM;
        }
        return LOW;
    }

    /** Returns the average number of requests a searching student sends each round. */
    public Estimate getRequestsPerRound() {
        return requestsPerRound;
    }

    /** Returns the share of requests left unanswered. */
    public Estimate getIgnoreRate() {
        return ignoreRate;
    }

    /** Returns the share of answered requests accepted at this match score. */
    public Estimate getAcceptRate(double mutualScore) {
        return acceptRateByBand[bandOf(mutualScore)];
    }

    /** Returns the accept rate for one band: 0 low, 1 medium, 2 high. */
    public Estimate getAcceptRateForBand(int band) {
        return acceptRateByBand[band];
    }

    /** Returns how much a busy inbox multiplies the chance of accepting. */
    public Estimate getBusyFactor() {
        return busyFactor;
    }
}
