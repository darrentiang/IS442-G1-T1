package com.studybuddy.engine.advisor;

import java.util.Comparator;

/** Orders suggestions best first: most good matches gained, then the biggest rise in best fit. */
public class SuggestionComparator implements Comparator<Suggestion> {

    /** Returns a negative number when a should come before b. */
    @Override
    public int compare(Suggestion a, Suggestion b) {
        if (a.getGained() != b.getGained()) {
            return Integer.compare(b.getGained(), a.getGained());
        }
        double riseA = a.getBestAfter() - a.getBestBefore();
        double riseB = b.getBestAfter() - b.getBestBefore();
        return Double.compare(riseB, riseA);
    }
}
