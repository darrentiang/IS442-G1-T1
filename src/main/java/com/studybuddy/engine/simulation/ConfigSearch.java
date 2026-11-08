package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;

/**
 * Searches for settings that get more students a usable buddy. It tries a grid of strategy,
 * congestion strength and minimum score combinations around the current config, screens them
 * quickly under learned behaviour, then gives the best few the full three-behaviour check and
 * recommends the first one that passes every check.
 */
public class ConfigSearch {

    /** Terms per combination in the quick screen. */
    public static final int SCREEN_TERMS = 30;
    /** How many of the best screened combinations get the full check. */
    public static final int SHORTLIST_SIZE = 2;

    private static final double[] CONGESTION_OPTIONS = {0.0, 0.5};
    private static final double MIN_SCORE_STEP = 0.1;
    private static final double LOWEST_SCORE = 0.0;
    private static final double HIGHEST_SCORE = 1.0;

    private final MatchingEngine engine;
    private final ConfigSimulator simulator;

    /** Creates a search that uses the given engine. */
    public ConfigSearch(MatchingEngine engine) {
        this.engine = engine;
        this.simulator = new ConfigSimulator(engine);
    }

    /**
     * Finds the best settings around the current config.
     *
     * @param state the app's students, requests and connections
     * @param current the config in use now
     * @return the recommendation; not found if nothing clearly beats the current config
     */
    public Recommendation recommend(CurrentState state, MatchingConfig current) {
        Baseline baseline = simulator.baseline(state, current);
        BehaviourModel learned = baseline.getBehaviours().get(0);
        ConfigOutcome currentScreen = simulator.simulate(state, current, current, learned, SCREEN_TERMS);

        List<MatchingConfig> candidates = buildGrid(current);
        List<MatchingConfig> passed = new ArrayList<>();
        List<Double> coverages = new ArrayList<>();
        for (MatchingConfig candidate : candidates) {
            ConfigOutcome outcome = simulator.simulate(state, candidate, current, learned, SCREEN_TERMS);
            BehaviourComparison check = new BehaviourComparison(learned, currentScreen, outcome);
            if (check.isQualityKept() && check.isInboxKept()) {
                insertByCoverage(passed, coverages, candidate, outcome.getCoverage().getMean());
            }
        }

        int shortlist = Math.min(SHORTLIST_SIZE, passed.size());
        for (int i = 0; i < shortlist; i++) {
            SimulationReport report = simulator.compare(baseline, state, current, passed.get(i));
            if (report.isProposedBetter()) {
                return new Recommendation(passed.get(i), report, candidates.size());
            }
        }
        return new Recommendation(null, null, candidates.size());
    }

    /**
     * Builds every combination of strategy, congestion strength (none, current, strong) and
     * minimum score (current, a step lower, a step higher), leaving out the current config itself.
     */
    private List<MatchingConfig> buildGrid(MatchingConfig current) {
        double[] congestions = {CONGESTION_OPTIONS[0], current.getCongestionStrength(), CONGESTION_OPTIONS[1]};
        double[] minScores = {
            current.getMinimumScore(),
            Math.max(LOWEST_SCORE, current.getMinimumScore() - MIN_SCORE_STEP),
            Math.min(HIGHEST_SCORE, current.getMinimumScore() + MIN_SCORE_STEP)
        };
        List<MatchingConfig> grid = new ArrayList<>();
        for (String strategy : engine.getStrategyNames()) {
            for (double congestion : congestions) {
                for (double minScore : minScores) {
                    MatchingConfig candidate = copyOf(current);
                    candidate.setActiveStrategyName(strategy);
                    candidate.setCongestionStrength(congestion);
                    candidate.setMinimumScore(minScore);
                    if (!sameSettings(candidate, current)) {
                        grid.add(candidate);
                    }
                }
            }
        }
        return grid;
    }

    /** Inserts a candidate so the list stays ordered from highest coverage to lowest. */
    private void insertByCoverage(List<MatchingConfig> configs, List<Double> coverages, MatchingConfig candidate,
            double coverage) {
        int position = 0;
        while (position < coverages.size() && coverages.get(position) >= coverage) {
            position++;
        }
        configs.add(position, candidate);
        coverages.add(position, coverage);
    }

    /** Returns true if the two configs have the same strategy, congestion strength and minimum score. */
    private boolean sameSettings(MatchingConfig first, MatchingConfig second) {
        return first.getActiveStrategyName().equals(second.getActiveStrategyName())
                && first.getCongestionStrength() == second.getCongestionStrength()
                && first.getMinimumScore() == second.getMinimumScore();
    }

    /** Returns a separate copy of a config, so changing the copy never touches the original. */
    private MatchingConfig copyOf(MatchingConfig original) {
        MatchingConfig copy = new MatchingConfig();
        copy.setActiveStrategyName(original.getActiveStrategyName());
        copy.setEnabledCriteria(original.getEnabledCriteria());
        copy.setWeights(original.getWeights());
        copy.setMaxResults(original.getMaxResults());
        copy.setMinimumScore(original.getMinimumScore());
        copy.setTargetUsableHours(original.getTargetUsableHours());
        copy.setMinSessionHours(original.getMinSessionHours());
        copy.setCongestionStrength(original.getCongestionStrength());
        return copy;
    }
}
