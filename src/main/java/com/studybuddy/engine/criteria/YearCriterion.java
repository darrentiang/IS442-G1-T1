package com.studybuddy.engine.criteria;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.StudentProfile;

/** Scores how close the pair are in year of study. */
public class YearCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "year";
    private static final String LABEL = "Year";

    private static final double SAME_YEAR = 1.0;
    private static final double ONE_APART = 0.6;
    private static final double FURTHER_APART = 0.2;
    private static final double NO_FIT = 0.0;
    private static final int NOT_SET = 0;
    private static final int SAME_YEAR_GAP = 0;
    private static final int ONE_YEAR_GAP = 1;

    /** Returns "year". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Year". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** 1.0 for the same year, 0.6 for one year apart, 0.2 for two or more. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        int seekerYear = seeker.getYearOfStudy();
        int candidateYear = candidate.getYearOfStudy();
        if (seekerYear <= NOT_SET || candidateYear <= NOT_SET) {
            return new CriterionScore(NO_FIT, null);
        }
        int gap = Math.abs(seekerYear - candidateYear);
        if (gap == SAME_YEAR_GAP) {
            return new CriterionScore(SAME_YEAR, "Both Year " + seekerYear);
        }
        if (gap == ONE_YEAR_GAP) {
            return new CriterionScore(ONE_APART, "One year apart");
        }
        return new CriterionScore(FURTHER_APART, null);
    }
}
