package com.studybuddy.engine.criteria;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyMode;

/** Scores whether the pair want to meet the same way: in person or online. */
public class StudyModeCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "studyMode";
    private static final String LABEL = "Study mode";

    private static final double SAME_MODE = 1.0;
    private static final double ONE_FLEXIBLE = 0.8;
    private static final double NO_FIT = 0.0;

    /** Returns "studyMode". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Study mode". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** 1.0 for the same mode, 0.8 if either is flexible, 0.0 for in person against online. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        if (seeker.getPreferences() == null || candidate.getPreferences() == null) {
            return new CriterionScore(NO_FIT, null);
        }
        StudyMode seekerMode = seeker.getPreferences().getMode();
        StudyMode candidateMode = candidate.getPreferences().getMode();
        if (seekerMode == null || candidateMode == null) {
            return new CriterionScore(NO_FIT, null);
        }
        if (seekerMode == StudyMode.EITHER || candidateMode == StudyMode.EITHER) {
            double value = seekerMode == candidateMode ? SAME_MODE : ONE_FLEXIBLE;
            return new CriterionScore(value, "Mode is flexible");
        }
        if (seekerMode == candidateMode) {
            if (seekerMode == StudyMode.IN_PERSON) {
                return new CriterionScore(SAME_MODE, "Both prefer in person");
            }
            return new CriterionScore(SAME_MODE, "Both prefer online");
        }
        return new CriterionScore(NO_FIT, null);
    }
}
