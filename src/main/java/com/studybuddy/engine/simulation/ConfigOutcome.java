package com.studybuddy.engine.simulation;

/** How one config did over many simulated terms: coverage, quality of new connections and peak inbox, each with a confidence interval. */
public class ConfigOutcome {

    private final Statistic coverage = new Statistic();
    private final Statistic averageScore = new Statistic();
    private final Statistic averageHours = new Statistic();
    private final Statistic peakInbox = new Statistic();

    /** Adds one term's numbers. */
    public void add(TermResult result) {
        coverage.add(result.getCoverage());
        averageScore.add(result.getAverageNewScore());
        averageHours.add(result.getAverageNewHours());
        peakInbox.add(result.getPeakInbox());
    }

    /** Returns the share of students with a usable buddy at the end, 0 to 1. */
    public Statistic getCoverage() {
        return coverage;
    }

    /** Returns the average match score of the connections made. */
    public Statistic getAverageScore() {
        return averageScore;
    }

    /** Returns the average usable hours a week of the connections made. */
    public Statistic getAverageHours() {
        return averageHours;
    }

    /** Returns the most requests any one student had waiting. */
    public Statistic getPeakInbox() {
        return peakInbox;
    }
}
