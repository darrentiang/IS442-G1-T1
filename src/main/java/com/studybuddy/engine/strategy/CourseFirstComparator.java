package com.studybuddy.engine.strategy;

import java.util.Comparator;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchResult;
import com.studybuddy.engine.criteria.CourseCriterion;

/** Orders results by the course criterion score, highest first; ties by mutual score. */
public class CourseFirstComparator implements Comparator<MatchResult> {

    private static final double NOT_SCORED = 0.0;

    private final MutualScoreComparator tieBreaker = new MutualScoreComparator();

    /** Compares two results so the better course fit comes first, then the higher mutual score. */
    @Override
    public int compare(MatchResult first, MatchResult second) {
        int byCourse = Double.compare(courseScore(second), courseScore(first));
        if (byCourse != 0) {
            return byCourse;
        }
        return tieBreaker.compare(first, second);
    }

    /** Returns the course score, or 0.0 when the course criterion was switched off. */
    private double courseScore(MatchResult result) {
        CriterionScore score = result.getCriterionScores().get(CourseCriterion.NAME);
        if (score == null) {
            return NOT_SCORED;
        }
        return score.getValue();
    }
}
