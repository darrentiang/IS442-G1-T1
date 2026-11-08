package com.studybuddy.engine.simulation;

import com.studybuddy.engine.MatchingConfig;

/**
 * The outcome of a search for better settings: the best config found and its full report, or
 * neither when nothing clearly beat the current config. Also says how many combinations were tried.
 */
public class Recommendation {

    private final MatchingConfig config;
    private final SimulationReport report;
    private final int combinationsTried;

    /**
     * Creates the recommendation.
     *
     * @param config the recommended config, or null if the current one is already the best found
     * @param report the full comparison for the recommended config, or null
     * @param combinationsTried how many configs the quick screen tried
     */
    public Recommendation(MatchingConfig config, SimulationReport report, int combinationsTried) {
        this.config = config;
        this.report = report;
        this.combinationsTried = combinationsTried;
    }

    /** Returns true if a config clearly better than the current one was found. */
    public boolean isFound() {
        return config != null;
    }

    /** Returns the recommended config, or null. */
    public MatchingConfig getConfig() {
        return config;
    }

    /** Returns the full comparison for the recommended config, or null. */
    public SimulationReport getReport() {
        return report;
    }

    /** Returns how many configs the quick screen tried. */
    public int getCombinationsTried() {
        return combinationsTried;
    }
}
