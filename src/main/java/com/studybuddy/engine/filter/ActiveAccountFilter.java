package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Student;

/** Removes suspended accounts and students who have no profile yet. */
public class ActiveAccountFilter implements CandidateFilter {

    private static final String NAME = "active-account";

    /** Returns "active-account". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Keeps the candidate only if their account is ACTIVE and they have a profile. */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context, MatchingConfig config) {
        return candidate.getStatus() == AccountStatus.ACTIVE && candidate.getProfile() != null;
    }
}
