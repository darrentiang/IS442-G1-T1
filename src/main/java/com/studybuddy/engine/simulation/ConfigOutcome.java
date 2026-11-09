package com.studybuddy.engine.simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * How one config did over many simulated terms: the share of students with a buddy they can meet,
 * and each student's chance of ending with one (the share of terms in which they did).
 */
public class ConfigOutcome {

    private final Statistic buddyCoverage = new Statistic();
    private final Map<String, Integer> termsWithBuddy = new HashMap<>();
    private int terms;

    /** Adds one term's numbers. */
    public void add(TermResult result) {
        terms++;
        buddyCoverage.add(result.getBuddyCoverage());
        for (String studentId : result.getStudentsWithBuddy()) {
            Integer count = termsWithBuddy.get(studentId);
            termsWithBuddy.put(studentId, count == null ? 1 : count + 1);
        }
    }

    /** Returns the share of students with a buddy they can meet, 0 to 1. */
    public Statistic getBuddyCoverage() {
        return buddyCoverage;
    }

    /**
     * Returns one student's chance of ending with a buddy they can meet.
     *
     * @param studentId the student's id
     * @return the share of terms in which they did, 0 to 1; 0 if no term was added
     */
    public double getChance(String studentId) {
        Integer count = termsWithBuddy.get(studentId);
        if (count == null || terms == 0) {
            return 0;
        }
        return (double) count / terms;
    }
}
