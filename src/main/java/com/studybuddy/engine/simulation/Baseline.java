package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The current config's simulated results, worked out once so several proposed configs can be
 * compared against it: the learned rates, the behaviours, and one outcome per behaviour.
 */
public class Baseline {

    private final BehaviourRates rates;
    private final List<BehaviourModel> behaviours;
    private final List<ConfigOutcome> outcomes;

    /**
     * Creates the baseline.
     *
     * @param rates the behaviour numbers learned from the app's data
     * @param behaviours learned, picky and eager behaviour, in that order
     * @param outcomes the current config's outcome under each behaviour, in the same order
     */
    public Baseline(BehaviourRates rates, List<BehaviourModel> behaviours, List<ConfigOutcome> outcomes) {
        this.rates = rates;
        this.behaviours = new ArrayList<>(behaviours);
        this.outcomes = new ArrayList<>(outcomes);
    }

    /** Returns the behaviour numbers learned from the app's data. */
    public BehaviourRates getRates() {
        return rates;
    }

    /** Returns the behaviours, learned first, as a read-only view. */
    public List<BehaviourModel> getBehaviours() {
        return Collections.unmodifiableList(behaviours);
    }

    /** Returns the current config's outcome under each behaviour, as a read-only view. */
    public List<ConfigOutcome> getOutcomes() {
        return Collections.unmodifiableList(outcomes);
    }
}
