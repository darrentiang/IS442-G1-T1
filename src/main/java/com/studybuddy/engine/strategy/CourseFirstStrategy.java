package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.MatchResult;

/** Ranks the people working on the same course first. */
public class CourseFirstStrategy extends AbstractMatchingStrategy {

    /** The config key for this strategy. */
    public static final String NAME = "course-first";
    private static final String LABEL = "Course first";
    private static final String DESCRIPTION = "Students in the same course rank highest.";

    /** Returns "course-first". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Course first". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** Returns "Students in the same course rank highest." */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /** Returns a comparator on the course score, then mutual score. */
    @Override
    protected Comparator<MatchResult> createComparator() {
        return new CourseFirstComparator();
    }
}
