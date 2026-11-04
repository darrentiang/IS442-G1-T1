package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.Student;

/** Removes the seeker themself from their own results. */
public class SelfFilter implements CandidateFilter {

    private static final String NAME = "self";

    /** Returns "self". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Keeps the candidate only if they are not the seeker. */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context, MatchingConfig config) {
        return !seeker.getId().equals(candidate.getId());
    }
}
