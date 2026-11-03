package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Orders results by usable hours together (uncapped), highest first; ties by mutual score. */
public class AvailabilityFirstComparator implements Comparator<MatchResult> {

    private final MutualScoreComparator tieBreaker = new MutualScoreComparator();

    /** Compares two results so more usable hours come first, then the higher mutual score. */
    @Override
    public int compare(MatchResult first, MatchResult second) {
        int byHours = Integer.compare(second.getUsableHours(), first.getUsableHours());
        if (byHours != 0) {
            return byHours;
        }
        return tieBreaker.compare(first, second);
    }
}
