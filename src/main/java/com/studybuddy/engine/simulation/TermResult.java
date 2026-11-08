package com.studybuddy.engine.simulation;

/**
 * The numbers from one simulated term: coverage at the end, the quality of the new
 * connections it produced, and the largest inbox any student had.
 */
public class TermResult {

    private final double coverage;
    private final int newConnections;
    private final double averageNewScore;
    private final double averageNewHours;
    private final int peakInbox;

    /**
     * Creates the result of one term.
     *
     * @param coverage share of students with at least one usable connection at the end, 0 to 1
     * @param newConnections connections made during the term
     * @param averageNewScore average mutual score of those connections, 0 if there were none
     * @param averageNewHours average usable hours a week of those connections, 0 if there were none
     * @param peakInbox the most requests any one student had waiting at once
     */
    public TermResult(double coverage, int newConnections, double averageNewScore, double averageNewHours,
            int peakInbox) {
        this.coverage = coverage;
        this.newConnections = newConnections;
        this.averageNewScore = averageNewScore;
        this.averageNewHours = averageNewHours;
        this.peakInbox = peakInbox;
    }

    /** Returns the share of students with at least one usable connection at the end, 0 to 1. */
    public double getCoverage() {
        return coverage;
    }

    /** Returns how many connections were made during the term. */
    public int getNewConnections() {
        return newConnections;
    }

    /** Returns the average mutual score of the connections made during the term. */
    public double getAverageNewScore() {
        return averageNewScore;
    }

    /** Returns the average usable hours a week of the connections made during the term. */
    public double getAverageNewHours() {
        return averageNewHours;
    }

    /** Returns the most requests any one student had waiting at once. */
    public int getPeakInbox() {
        return peakInbox;
    }
}
