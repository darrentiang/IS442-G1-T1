package com.studybuddy.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.studybuddy.model.Student;

/**
 * One row of a ranked result: who, the mutual score, both one-way fits, the per-criterion
 * breakdown (drawn as the on-screen score bars) and the reasons shown on the match card.
 */
public class MatchResult {

    private final Student candidate;
    private final double mutualScore;
    private final double fitForSeeker;
    private final double fitForCandidate;
    private final Map<String, CriterionScore> criterionScores;
    private final int usableHours;
    private final int pendingRequests;
    private final List<String> reasons;

    /**
     * Creates one result row. The map and list are copied and cannot be changed afterwards.
     *
     * @param candidate the student this row is about
     * @param mutualScore the final score, after the congestion adjustment
     * @param fitForSeeker how well the candidate suits the seeker, 0.0 to 1.0
     * @param fitForCandidate how well the seeker suits the candidate, 0.0 to 1.0
     * @param criterionScores the seeker-side score per criterion name
     * @param usableHours hours per week both are free in blocks long enough for a session
     * @param pendingRequests how many PENDING requests the candidate has received
     * @param reasons the short reasons shown on the match card
     */
    public MatchResult(Student candidate, double mutualScore, double fitForSeeker, double fitForCandidate,
            Map<String, CriterionScore> criterionScores, int usableHours, int pendingRequests,
            List<String> reasons) {
        this.candidate = candidate;
        this.mutualScore = mutualScore;
        this.fitForSeeker = fitForSeeker;
        this.fitForCandidate = fitForCandidate;
        this.criterionScores = Collections.unmodifiableMap(new LinkedHashMap<>(criterionScores));
        this.usableHours = usableHours;
        this.pendingRequests = pendingRequests;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
    }

    /** Returns the candidate this row is about. */
    public Student getCandidate() {
        return candidate;
    }

    /** Returns the final score both ways, after the congestion adjustment. */
    public double getMutualScore() {
        return mutualScore;
    }

    /** Returns how well the candidate suits the seeker, 0.0 to 1.0. */
    public double getFitForSeeker() {
        return fitForSeeker;
    }

    /** Returns how well the seeker suits the candidate, 0.0 to 1.0. */
    public double getFitForCandidate() {
        return fitForCandidate;
    }

    /** Returns the seeker-side score per criterion name (read-only; drawn as score bars). */
    public Map<String, CriterionScore> getCriterionScores() {
        return criterionScores;
    }

    /** Returns the hours per week both are free in blocks long enough for a session. */
    public int getUsableHours() {
        return usableHours;
    }

    /** Returns how many PENDING requests the candidate has received. */
    public int getPendingRequests() {
        return pendingRequests;
    }

    /** Returns the short reasons shown on the match card (read-only). */
    public List<String> getReasons() {
        return reasons;
    }
}
