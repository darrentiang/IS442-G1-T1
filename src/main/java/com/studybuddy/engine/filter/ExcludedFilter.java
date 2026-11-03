package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.model.Student;

/**
 * Removes students the seeker should not be offered again: already connected, a pending
 * request either way, or they declined the seeker before. The service decides who is listed.
 */
public class ExcludedFilter implements CandidateFilter {

    private static final String NAME = "excluded";

    /** Returns "excluded". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Keeps the candidate only if their id is not in the context's excluded ids. */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context) {
        return !context.getExcludedIds().contains(candidate.getId());
    }
}
