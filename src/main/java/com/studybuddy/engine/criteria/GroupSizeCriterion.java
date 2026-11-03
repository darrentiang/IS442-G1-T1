package com.studybuddy.engine.criteria;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.StudentProfile;

/** Scores whether the pair want the same group size: one-to-one or a small group. */
public class GroupSizeCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "groupSize";
    private static final String LABEL = "Group size";

    private static final double SAME_FORMAT = 1.0;
    private static final double ONE_FLEXIBLE = 0.8;
    private static final double DIFFERENT_FORMAT = 0.3;
    private static final double NO_FIT = 0.0;

    /** Returns "groupSize". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Group size". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** 1.0 for the same format, 0.8 if either is flexible, 0.3 for one-to-one against small group. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        if (seeker.getPreferences() == null || candidate.getPreferences() == null) {
            return new CriterionScore(NO_FIT, null);
        }
        GroupFormat seekerFormat = seeker.getPreferences().getFormat();
        GroupFormat candidateFormat = candidate.getPreferences().getFormat();
        if (seekerFormat == null || candidateFormat == null) {
            return new CriterionScore(NO_FIT, null);
        }
        if (seekerFormat == GroupFormat.EITHER || candidateFormat == GroupFormat.EITHER) {
            double value = seekerFormat == candidateFormat ? SAME_FORMAT : ONE_FLEXIBLE;
            return new CriterionScore(value, "Group size is flexible");
        }
        if (seekerFormat == candidateFormat) {
            if (seekerFormat == GroupFormat.ONE_TO_ONE) {
                return new CriterionScore(SAME_FORMAT, "Both prefer one-to-one");
            }
            return new CriterionScore(SAME_FORMAT, "Both prefer small groups");
        }
        return new CriterionScore(DIFFERENT_FORMAT, null);
    }
}
