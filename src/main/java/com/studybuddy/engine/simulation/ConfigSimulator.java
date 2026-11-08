package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;

/**
 * Tests a proposed config against the current one: learns behaviour from the app's requests,
 * then simulates many terms of each config under learned, picky and eager behaviour. Both
 * configs use the same random seeds, so any difference comes from the config, not from luck.
 */
public class ConfigSimulator {

    /** How many rounds of requests each term plays out. */
    public static final int ROUNDS = 3;
    /** How many terms are simulated per config and behaviour. */
    public static final int TERMS = 200;
    /** The first random seed; term i uses FIRST_SEED + i, so results are repeatable. */
    public static final long FIRST_SEED = 442L;

    private final MatchingEngine engine;

    /** Creates a simulator that uses the given engine. */
    public ConfigSimulator(MatchingEngine engine) {
        this.engine = engine;
    }

    /**
     * Compares a proposed config with the current one.
     *
     * @param state the app's students, requests and connections
     * @param current the config in use now; also the yardstick every pair is judged by
     * @param proposed the config being tested
     * @return the report, with one comparison per behaviour
     */
    public SimulationReport compare(CurrentState state, MatchingConfig current, MatchingConfig proposed) {
        return compare(baseline(state, current), state, current, proposed);
    }

    /**
     * Learns behaviour and simulates the current config once, so it can be reused for many comparisons.
     *
     * @param state the app's students, requests and connections
     * @param current the config in use now
     * @return the baseline
     */
    public Baseline baseline(CurrentState state, MatchingConfig current) {
        BehaviourRates rates = new BehaviourCalibrator(engine).calibrate(state.getRequests(), current);
        List<BehaviourModel> behaviours = new ArrayList<>();
        behaviours.add(new LearnedBehaviour(rates));
        behaviours.add(new PickyBehaviour(rates));
        behaviours.add(new EagerBehaviour(rates));
        List<ConfigOutcome> outcomes = new ArrayList<>();
        for (BehaviourModel behaviour : behaviours) {
            outcomes.add(simulate(state, current, current, behaviour, TERMS));
        }
        return new Baseline(rates, behaviours, outcomes);
    }

    /**
     * Compares a proposed config with an already simulated baseline.
     *
     * @param baseline the current config's results
     * @param state the app's students, requests and connections
     * @param current the config in use now; also the yardstick every pair is judged by
     * @param proposed the config being tested
     * @return the report, with one comparison per behaviour
     */
    public SimulationReport compare(Baseline baseline, CurrentState state, MatchingConfig current,
            MatchingConfig proposed) {
        List<BehaviourComparison> comparisons = new ArrayList<>();
        List<BehaviourModel> behaviours = baseline.getBehaviours();
        for (int i = 0; i < behaviours.size(); i++) {
            BehaviourModel behaviour = behaviours.get(i);
            ConfigOutcome proposedOutcome = simulate(state, proposed, current, behaviour, TERMS);
            comparisons.add(new BehaviourComparison(behaviour, baseline.getOutcomes().get(i), proposedOutcome));
        }
        return new SimulationReport(baseline.getRates(), comparisons, ROUNDS, TERMS);
    }

    /**
     * Simulates a number of terms of one config under one behaviour.
     *
     * @param state the app's students, requests and connections
     * @param config the config being simulated
     * @param yardstick the config every pair is judged by
     * @param behaviour how simulated students behave
     * @param terms how many terms to simulate
     * @return the combined outcome
     */
    public ConfigOutcome simulate(CurrentState state, MatchingConfig config, MatchingConfig yardstick,
            BehaviourModel behaviour, int terms) {
        TermSimulator simulator = new TermSimulator(engine, config, yardstick, behaviour, ROUNDS);
        ConfigOutcome outcome = new ConfigOutcome();
        for (int term = 0; term < terms; term++) {
            outcome.add(simulator.run(state.getStudents(), state.getRequests(), state.getConnections(),
                    FIRST_SEED + term));
        }
        return outcome;
    }
}
