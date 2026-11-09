package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of simulating a proposed config next to the saved one: the behaviour numbers used and
 * one comparison per behaviour (learned, picky, eager). It reports what changes for students and
 * leaves judging it to the admin.
 */
public class SimulationReport {

    private final BehaviourRates rates;
    private final List<BehaviourComparison> comparisons;
    private final int rounds;
    private final int terms;

    /**
     * Creates the report.
     *
     * @param rates the behaviour numbers learned from the app's data
     * @param comparisons one comparison per behaviour, learned first
     * @param rounds how many rounds each term played out
     * @param terms how many terms were simulated per config and behaviour
     */
    public SimulationReport(BehaviourRates rates, List<BehaviourComparison> comparisons, int rounds, int terms) {
        this.rates = rates;
        this.comparisons = new ArrayList<>(comparisons);
        this.rounds = rounds;
        this.terms = terms;
    }

    /** Returns the behaviour numbers learned from the app's data. */
    public BehaviourRates getRates() {
        return rates;
    }

    /** Returns one comparison per behaviour, learned first, as a read-only view. */
    public List<BehaviourComparison> getComparisons() {
        return Collections.unmodifiableList(comparisons);
    }

    /** Returns the comparison under learned behaviour, the headline one. */
    public BehaviourComparison getHeadline() {
        return comparisons.get(0);
    }

    /** Returns how many rounds each term played out. */
    public int getRounds() {
        return rounds;
    }

    /** Returns how many terms were simulated per config and behaviour. */
    public int getTerms() {
        return terms;
    }
}
