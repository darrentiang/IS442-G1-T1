package com.studybuddy.engine.criteria;

import java.util.Set;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;

/** Scores how many of the seeker's goals the candidate also has. */
public class StudyGoalCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "studyGoal";
    private static final String LABEL = "Study goal";

    private static final double NO_FIT = 0.0;
    private static final double REASON_THRESHOLD = 0.6;

    /** Returns "studyGoal". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Study goal". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** Shared goals divided by the seeker's number of goals (seeker-side coverage). */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        if (seeker.getPreferences() == null || candidate.getPreferences() == null) {
            return new CriterionScore(NO_FIT, null);
        }
        Set<StudyGoal> seekerGoals = seeker.getPreferences().getGoals();
        Set<StudyGoal> candidateGoals = candidate.getPreferences().getGoals();
        if (seekerGoals == null || candidateGoals == null || seekerGoals.isEmpty()) {
            return new CriterionScore(NO_FIT, null);
        }
        int shared = 0;
        StringBuilder sharedText = new StringBuilder();
        for (StudyGoal goal : StudyGoal.values()) {
            if (seekerGoals.contains(goal) && candidateGoals.contains(goal)) {
                if (shared > 0) {
                    sharedText.append(" and ");
                }
                sharedText.append(readable(goal));
                shared++;
            }
        }
        double value = (double) shared / seekerGoals.size();
        String reason = null;
        if (value >= REASON_THRESHOLD) {
            reason = "Both doing " + sharedText;
        }
        return new CriterionScore(value, reason);
    }

    /** Turns a goal into readable lower-case text, e.g. EXAM_PREP into "exam prep". */
    private String readable(StudyGoal goal) {
        return goal.getLabel().toLowerCase();
    }
}
