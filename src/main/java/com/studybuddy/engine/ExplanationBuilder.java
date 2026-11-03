package com.studybuddy.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Picks the short reasons shown on a match card, so students can see why someone was suggested. */
public class ExplanationBuilder {

    private static final int MAX_CRITERION_REASONS = 2;
    private static final int BUSY_THRESHOLD = 3;
    private static final double NO_WEIGHT = 0.0;
    private static final double NOTHING_CHOSEN = -1.0;

    /**
     * Builds the reasons: up to two from the criteria that matter most (highest weight times
     * score, among those with a reason), then a note if the candidate is already busy.
     *
     * @param scores the seeker-side score per criterion name
     * @param pendingIncoming how many PENDING requests the candidate has received
     * @param config the admin's settings, which hold the weights
     * @return the reasons, best first
     */
    public List<String> build(Map<String, CriterionScore> scores, int pendingIncoming, MatchingConfig config) {
        List<String> reasons = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (int i = 0; i < MAX_CRITERION_REASONS; i++) {
            String bestName = null;
            double bestImpact = NOTHING_CHOSEN;
            for (Map.Entry<String, CriterionScore> entry : scores.entrySet()) {
                CriterionScore score = entry.getValue();
                if (score.getReason() == null || used.contains(entry.getKey())) {
                    continue;
                }
                double impact = weightOf(entry.getKey(), config) * score.getValue();
                if (impact > bestImpact) {
                    bestImpact = impact;
                    bestName = entry.getKey();
                }
            }
            if (bestName == null) {
                break;
            }
            used.add(bestName);
            reasons.add(scores.get(bestName).getReason());
        }
        if (pendingIncoming >= BUSY_THRESHOLD) {
            reasons.add(pendingIncoming + " pending requests");
        }
        return reasons;
    }

    /** Returns the configured weight for a criterion, or 0 when it has none. */
    private double weightOf(String criterionName, MatchingConfig config) {
        if (config.getWeights() == null) {
            return NO_WEIGHT;
        }
        Double weight = config.getWeights().get(criterionName);
        if (weight == null) {
            return NO_WEIGHT;
        }
        return weight;
    }
}
