package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Ranks purely by mutual score: the best all-round match comes first. */
public class BalancedStrategy extends AbstractMatchingStrategy {

    /** The config key for this strategy. */
    public static final String NAME = "balanced";
    private static final String LABEL = "Balanced";

    /** Returns "balanced". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Balanced". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** Returns a comparator on mutual score. */
    @Override
    protected Comparator<MatchResult> createComparator() {
        return new MutualScoreComparator();
    }
}
