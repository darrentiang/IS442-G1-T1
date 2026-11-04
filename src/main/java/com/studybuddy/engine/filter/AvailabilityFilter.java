package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.Student;

/** Applies the Free together search option: keeps candidates with enough usable hours in common. */
public class AvailabilityFilter implements CandidateFilter {

    private static final String NAME = "availability";
    private static final int NO_LIMIT = 0;

    /** Returns "availability". */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Keeps everyone when no minimum is set; otherwise keeps candidates whose usable hours with
     * the seeker (shared blocks of at least minSessionHours) reach the minimum.
     */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context, MatchingConfig config) {
        if (context.getMinUsableHours() <= NO_LIMIT) {
            return true;
        }
        if (seeker.getProfile() == null || candidate.getProfile() == null) {
            return false;
        }
        AvailabilityGrid seekerGrid = seeker.getProfile().getAvailability();
        AvailabilityGrid candidateGrid = candidate.getProfile().getAvailability();
        if (seekerGrid == null || candidateGrid == null) {
            return false;
        }
        return seekerGrid.usableHours(candidateGrid, config.getMinSessionHours()) >= context.getMinUsableHours();
    }
}
