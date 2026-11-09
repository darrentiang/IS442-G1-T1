package com.studybuddy.engine.simulation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** The numbers from one simulated term: which students ended with a buddy they can meet, and how many that is. */
public class TermResult {

    private final double buddyCoverage;
    private final Set<String> studentsWithBuddy;

    /**
     * Creates the result of one term.
     *
     * @param buddyCoverage share of students with a buddy they can meet at the end, 0 to 1
     * @param studentsWithBuddy ids of the students who ended with a buddy they can meet
     */
    public TermResult(double buddyCoverage, Set<String> studentsWithBuddy) {
        this.buddyCoverage = buddyCoverage;
        this.studentsWithBuddy = new HashSet<>(studentsWithBuddy);
    }

    /** Returns the share of students with a buddy they can meet at the end, 0 to 1. */
    public double getBuddyCoverage() {
        return buddyCoverage;
    }

    /** Returns the ids of the students who ended with a buddy they can meet, as a read-only view. */
    public Set<String> getStudentsWithBuddy() {
        return Collections.unmodifiableSet(studentsWithBuddy);
    }
}
