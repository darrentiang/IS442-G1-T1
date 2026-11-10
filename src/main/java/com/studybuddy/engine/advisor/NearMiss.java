package com.studybuddy.engine.advisor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.model.Student;

/**
 * Someone who almost matched the student (scored just under "good"), with the pair's criterion
 * scores from both sides: how well they suit her, and how well she suits them.
 */
public class NearMiss {

    private final Student candidate;
    private final Map<String, CriterionScore> herSide;
    private final Map<String, CriterionScore> theirSide;

    /**
     * Creates the near miss.
     *
     * @param candidate the student who almost matched
     * @param herSide criterion name to score, judged from the advised student's side
     * @param theirSide criterion name to score, judged from the candidate's side
     */
    public NearMiss(Student candidate, Map<String, CriterionScore> herSide, Map<String, CriterionScore> theirSide) {
        this.candidate = candidate;
        this.herSide = new LinkedHashMap<>(herSide);
        this.theirSide = new LinkedHashMap<>(theirSide);
    }

    /** Returns the student who almost matched. */
    public Student getCandidate() {
        return candidate;
    }

    /** Returns the criterion scores from the advised student's side, as a read-only view. */
    public Map<String, CriterionScore> getHerSide() {
        return Collections.unmodifiableMap(herSide);
    }

    /** Returns the criterion scores from the candidate's side, as a read-only view. */
    public Map<String, CriterionScore> getTheirSide() {
        return Collections.unmodifiableMap(theirSide);
    }
}
