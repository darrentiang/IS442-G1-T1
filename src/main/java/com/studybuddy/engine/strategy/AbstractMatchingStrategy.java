package com.studybuddy.engine.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.studybuddy.engine.MatchResult;

/**
 * Template Method pattern: rank() is the fixed algorithm (copy the list, sort the copy) and
 * each subclass fills in the one step that differs, the comparator that decides the order.
 */
public abstract class AbstractMatchingStrategy implements MatchingStrategy {

    /** Copies the results and sorts the copy with this strategy's comparator. */
    @Override
    public List<MatchResult> rank(List<MatchResult> scored) {
        List<MatchResult> copy = new ArrayList<>(scored);
        Collections.sort(copy, createComparator());
        return copy;
    }

    /** Returns the comparator that puts the best result first. */
    protected abstract Comparator<MatchResult> createComparator();
}
