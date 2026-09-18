package com.studybuddy.engine;

import com.studybuddy.model.StudentProfile;

/** One scoring rule. One class per criterion. Returns 0.0 (no fit) to 1.0 (perfect). */
public interface MatchCriterion {

    /** Returns the criterion name, e.g. "availability" — used as the config key. */
    String getName();

    /** Scores how well two profiles fit each other, from 0.0 (no fit) to 1.0 (perfect). */
    double score(StudentProfile a, StudentProfile b);
}
