package com.studybuddy.engine.advisor;

/**
 * One verified suggestion: a change, how many near misses it was aimed at, and what the real engine
 * says it does to the student's good matches and best fit. Never changes once made.
 */
public class Suggestion {

    private final PreferenceChange change;
    private final int peopleHeldBack;
    private final int goodBefore;
    private final int goodAfter;
    private final double bestBefore;
    private final double bestAfter;

    /**
     * Creates the suggestion.
     *
     * @param change the change being suggested
     * @param peopleHeldBack how many near misses this preference held back
     * @param goodBefore good matches with the student's current preferences
     * @param goodAfter good matches if the change is made
     * @param bestBefore the best mutual score now, 0 to 1
     * @param bestAfter the best mutual score after the change, 0 to 1
     */
    public Suggestion(PreferenceChange change, int peopleHeldBack, int goodBefore, int goodAfter,
            double bestBefore, double bestAfter) {
        this.change = change;
        this.peopleHeldBack = peopleHeldBack;
        this.goodBefore = goodBefore;
        this.goodAfter = goodAfter;
        this.bestBefore = bestBefore;
        this.bestAfter = bestAfter;
    }

    /** Returns the change being suggested. */
    public PreferenceChange getChange() {
        return change;
    }

    /** Returns how many near misses this preference held back. */
    public int getPeopleHeldBack() {
        return peopleHeldBack;
    }

    /** Returns the good matches now. */
    public int getGoodBefore() {
        return goodBefore;
    }

    /** Returns the good matches after the change. */
    public int getGoodAfter() {
        return goodAfter;
    }

    /** Returns how many good matches the change adds. */
    public int getGained() {
        return goodAfter - goodBefore;
    }

    /** Returns the best mutual score now, 0 to 1. */
    public double getBestBefore() {
        return bestBefore;
    }

    /** Returns the best mutual score after the change, 0 to 1. */
    public double getBestAfter() {
        return bestAfter;
    }

    /** Returns why this change is suggested, e.g. "3 people almost matched you but were held back by study mode". */
    public String getReason() {
        return peopleHeldBack + (peopleHeldBack == 1 ? " person almost matched you but was" : " people almost matched you but were")
                + " held back by " + change.getBlockerLabel();
    }
}
