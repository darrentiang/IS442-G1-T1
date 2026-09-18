package com.studybuddy.engine;

import java.util.Map;

import com.studybuddy.model.Student;

/**
 * One row of a ranked result: who, the total, and the per-criterion breakdown
 * (the breakdown is what the score bars on screen are drawn from).
 */
public class MatchResult {

    private final Student candidate;
    private final double totalScore;
    private final Map<String, Double> criterionScores;

    /** Creates one result row for a ranked candidate. */
    public MatchResult(Student candidate, double totalScore, Map<String, Double> criterionScores) {
        this.candidate = candidate;
        this.totalScore = totalScore;
        this.criterionScores = criterionScores;
    }

    /** Returns the candidate this row is about. */
    public Student getCandidate() {
        return candidate;
    }

    /** Returns the combined score across all criteria. */
    public double getTotalScore() {
        return totalScore;
    }

    /** Returns the score per criterion name (drawn as the on-screen score bars). */
    public Map<String, Double> getCriterionScores() {
        return criterionScores;
    }
}
