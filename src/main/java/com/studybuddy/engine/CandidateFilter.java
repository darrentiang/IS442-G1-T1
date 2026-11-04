package com.studybuddy.engine;

import com.studybuddy.model.Student;

/** A dealbreaker check: removes candidates who can never be a match, before any scoring. */
public interface CandidateFilter {

    /** Returns the filter name, e.g. "self". */
    String getName();

    /**
     * Decides whether the candidate stays in the pool.
     *
     * @param seeker the student looking for a buddy
     * @param candidate the student being considered
     * @param context the search details for this request
     * @param config the admin's settings, e.g. the shortest session length
     * @return true to keep the candidate, false to remove them
     */
    boolean accepts(Student seeker, Student candidate, MatchContext context, MatchingConfig config);
}
