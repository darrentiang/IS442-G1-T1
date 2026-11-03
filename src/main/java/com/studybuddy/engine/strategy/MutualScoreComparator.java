package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Orders results by mutual score, highest first. */
public class MutualScoreComparator implements Comparator<MatchResult> {

    /** Compares two results so the higher mutual score comes first. */
    @Override
    public int compare(MatchResult first, MatchResult second) {
        return Double.compare(second.getMutualScore(), first.getMutualScore());
    }
}
