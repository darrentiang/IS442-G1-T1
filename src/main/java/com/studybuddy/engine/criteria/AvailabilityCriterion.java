package com.studybuddy.engine.criteria;

import java.util.List;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.TimeBlock;

/**
 * Scores how much time the pair could really study together. Only shared blocks long enough
 * for a session count, so scattered single hours do not inflate the score.
 */
public class AvailabilityCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "availability";
    private static final String LABEL = "Availability";

    private static final double FULL_SCORE = 1.0;
    private static final double NO_FIT = 0.0;
    private static final double REASON_THRESHOLD = 0.6;

    /** Returns "availability". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Availability". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** Scores usable hours together against the target, capped at 1.0. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        AvailabilityGrid seekerGrid = seeker.getAvailability();
        AvailabilityGrid candidateGrid = candidate.getAvailability();
        if (seekerGrid == null || candidateGrid == null) {
            return new CriterionScore(NO_FIT, null);
        }
        List<TimeBlock> blocks = seekerGrid.commonBlocks(candidateGrid, config.getMinSessionHours());
        int usableHours = 0;
        for (TimeBlock block : blocks) {
            usableHours += block.getLength();
        }
        double value;
        if (config.getTargetUsableHours() <= 0) {
            value = usableHours > 0 ? FULL_SCORE : NO_FIT;
        } else {
            value = Math.min(FULL_SCORE, (double) usableHours / config.getTargetUsableHours());
        }
        String reason = null;
        if (value >= REASON_THRESHOLD) {
            reason = "Free together " + joinBlocks(blocks);
        }
        return new CriterionScore(value, reason);
    }

    /** Joins the blocks as text, e.g. "WED 19-21, SAT 14-17". */
    private String joinBlocks(List<TimeBlock> blocks) {
        StringBuilder text = new StringBuilder();
        for (TimeBlock block : blocks) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(block);
        }
        return text.toString();
    }
}
