package com.studybuddy.engine.strategy;

import java.util.List;

import com.studybuddy.engine.MatchResult;

/** One way of ordering scored results. The admin picks the active one on the config page. */
public interface MatchingStrategy {

    /** Returns the strategy name, e.g. "balanced", used as the config key. */
    String getName();

    /** Returns the name shown to people, e.g. "Balanced". */
    String getLabel();

    /**
     * Orders the scored results, best first.
     *
     * @param scored the results to order; this list is not changed
     * @return a new list in ranked order
     */
    List<MatchResult> rank(List<MatchResult> scored);
}
