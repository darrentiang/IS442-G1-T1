package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Ranks the people you can actually meet most often first. */
public class AvailabilityFirstStrategy extends AbstractMatchingStrategy {

    /** The config key for this strategy. */
    public static final String NAME = "availability-first";
    private static final String LABEL = "Availability first";

    /** Returns "availability-first". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Availability first". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** Returns a comparator on usable hours, then mutual score. */
    @Override
    protected Comparator<MatchResult> createComparator() {
        return new AvailabilityFirstComparator();
    }
}
