package com.studybuddy.engine.criteria;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.Intensity;
import com.studybuddy.model.StudentProfile;

/** Scores how closely the pair's study intensity lines up on CASUAL, STEADY, GRIND. */
public class IntensityCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "intensity";
    private static final String LABEL = "Intensity";

    private static final double FULL_SCORE = 1.0;
    private static final double PENALTY_PER_STEP = 0.5;
    private static final double NO_FIT = 0.0;
    private static final double REASON_THRESHOLD = 0.6;

    /** Returns "intensity". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Intensity". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** 1.0 minus 0.5 for each step apart on CASUAL, STEADY, GRIND. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        if (seeker.getPreferences() == null || candidate.getPreferences() == null) {
            return new CriterionScore(NO_FIT, null);
        }
        Intensity seekerIntensity = seeker.getPreferences().getIntensity();
        Intensity candidateIntensity = candidate.getPreferences().getIntensity();
        if (seekerIntensity == null || candidateIntensity == null) {
            return new CriterionScore(NO_FIT, null);
        }
        int steps = Math.abs(seekerIntensity.ordinal() - candidateIntensity.ordinal());
        double value = Math.max(NO_FIT, FULL_SCORE - PENALTY_PER_STEP * steps);
        String reason = null;
        if (value >= REASON_THRESHOLD) {
            reason = describe(seekerIntensity);
        }
        return new CriterionScore(value, reason);
    }

    /** Returns the reason for a pair who share this intensity. */
    private String describe(Intensity intensity) {
        if (intensity == Intensity.CASUAL) {
            return "Both studying casually";
        }
        if (intensity == Intensity.STEADY) {
            return "Both studying steadily";
        }
        return "Both studying intensively";
    }
}
