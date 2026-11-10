package com.studybuddy.engine.advisor;

/**
 * The admin's BOSS Advisor settings: when a match counts as good, and how many good matches a
 * student needs before they stop seeing suggestions. Kept apart from MatchingConfig, which decides
 * how students are ranked; these decide who needs help.
 */
public class AdvisorSettings {

    private double goodMatchScore;
    private int enoughGoodMatches;

    /**
     * Creates the settings.
     *
     * @param goodMatchScore the lowest mutual score that counts as a good match, 0 to 1
     * @param enoughGoodMatches how many good matches a student needs to not be stranded
     */
    public AdvisorSettings(double goodMatchScore, int enoughGoodMatches) {
        this.goodMatchScore = goodMatchScore;
        this.enoughGoodMatches = enoughGoodMatches;
    }

    /** Returns the lowest mutual score that counts as a good match, 0 to 1. */
    public double getGoodMatchScore() {
        return goodMatchScore;
    }

    /** Sets the lowest mutual score that counts as a good match, 0 to 1. */
    public void setGoodMatchScore(double goodMatchScore) {
        this.goodMatchScore = goodMatchScore;
    }

    /** Returns how many good matches a student needs to not be stranded. */
    public int getEnoughGoodMatches() {
        return enoughGoodMatches;
    }

    /** Sets how many good matches a student needs to not be stranded. */
    public void setEnoughGoodMatches(int enoughGoodMatches) {
        this.enoughGoodMatches = enoughGoodMatches;
    }
}
