package com.studybuddy.engine;

import com.studybuddy.model.StudentProfile;

/** One scoring rule. One class per criterion. Scores a pair from 0.0 (no fit) to 1.0 (perfect). */
public interface MatchCriterion {

    /** Returns the criterion name, e.g. "availability", used as the config key. */
    String getName();

    /** Returns the name shown to people, e.g. "Availability". */
    String getLabel();

    /**
     * Scores how well the candidate suits the seeker, judged from the seeker's side only.
     * Missing data gives a score of 0.0 with no reason; this method never throws.
     *
     * @param seeker the profile of the student looking for a buddy
     * @param candidate the profile of the student being considered
     * @param config the admin's settings
     * @return the score and an optional reason
     */
    CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config);
}
