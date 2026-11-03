package com.studybuddy.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studybuddy.model.StudentProfile;

/**
 * Turns criterion scores into one mutual score. Each side's fit is the weighted average of
 * its criterion scores; the two fits are combined with the harmonic mean, because a match
 * must work for both people: a one-sided match (say 0.9 for one, 0.3 for the other) is pulled
 * down towards the weaker side (0.45) instead of being averaged up (0.6).
 */
public class ReciprocalScorer {

    private static final double NO_WEIGHT = 0.0;
    private static final double NO_FIT = 0.0;
    private static final double HARMONIC_FACTOR = 2.0;

    /**
     * Scores one direction with every given criterion.
     *
     * @param from the student whose side the scores are judged from
     * @param to the student being judged
     * @param criteria the criteria to apply, in order
     * @param config the admin's settings
     * @return criterion name to score, in the order of the criteria
     */
    public Map<String, CriterionScore> scoreSide(StudentProfile from, StudentProfile to,
            List<MatchCriterion> criteria, MatchingConfig config) {
        Map<String, CriterionScore> scores = new LinkedHashMap<>();
        for (MatchCriterion criterion : criteria) {
            scores.put(criterion.getName(), criterion.score(from, to, config));
        }
        return scores;
    }

    /**
     * Returns the weighted average of the scores. Weights are normalised over the criteria
     * present, a missing weight counts as 0, and if every weight is 0 the fit is 0.
     *
     * @param scores criterion name to score, for the enabled criteria only
     * @param config the admin's settings, which hold the weights
     * @return the fit, from 0.0 to 1.0
     */
    public double weightedFit(Map<String, CriterionScore> scores, MatchingConfig config) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        for (Map.Entry<String, CriterionScore> entry : scores.entrySet()) {
            double weight = weightOf(entry.getKey(), config);
            weightedSum += weight * entry.getValue().getValue();
            totalWeight += weight;
        }
        if (totalWeight <= NO_WEIGHT) {
            return NO_FIT;
        }
        return weightedSum / totalWeight;
    }

    /**
     * Combines the two one-way fits with the harmonic mean, 2xy / (x + y).
     *
     * @param fitForSeeker how well the candidate suits the seeker
     * @param fitForCandidate how well the seeker suits the candidate
     * @return the mutual score, or 0.0 when both fits are 0
     */
    public double mutualScore(double fitForSeeker, double fitForCandidate) {
        double sum = fitForSeeker + fitForCandidate;
        if (sum <= NO_FIT) {
            return NO_FIT;
        }
        return HARMONIC_FACTOR * fitForSeeker * fitForCandidate / sum;
    }

    /** Returns the configured weight for a criterion, or 0 when it has none. */
    private double weightOf(String criterionName, MatchingConfig config) {
        if (config.getWeights() == null) {
            return NO_WEIGHT;
        }
        Double weight = config.getWeights().get(criterionName);
        if (weight == null || weight < NO_WEIGHT) {
            return NO_WEIGHT;
        }
        return weight;
    }
}
