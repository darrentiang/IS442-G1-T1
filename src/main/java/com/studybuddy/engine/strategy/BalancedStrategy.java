package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Ranks purely by mutual score: the best all-round match comes first. */
public class BalancedStrategy extends AbstractMatchingStrategy {

    /** The config key for this strategy. */
    public static final String NAME = "balanced";
    private static final String LABEL = "Balanced";
    private static final String DESCRIPTION = "Every criterion counts by its weight.";

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

    /** Returns "Every criterion counts by its weight." */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /** Returns a comparator on mutual score. */
    @Override
    protected Comparator<MatchResult> createComparator() {
        return new MutualScoreComparator();
    }
}
