package com.studybuddy.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** The admin's settings: which strategy, which criteria on, what weights. */
public class MatchingConfig {

    private String activeStrategyName;
    private Set<String> enabledCriteria = new HashSet<>();
    private Map<String, Double> weights = new HashMap<>();
    private int maxResults;
    private double minimumScore;

    /** Returns the name of the strategy currently in use. */
    public String getActiveStrategyName() {
        return activeStrategyName;
    }

    /** Changes the strategy currently in use. */
    public void setActiveStrategyName(String activeStrategyName) {
        this.activeStrategyName = activeStrategyName;
    }

    /** Returns the names of the criteria that are switched on. */
    public Set<String> getEnabledCriteria() {
        return enabledCriteria;
    }

    /** Replaces the set of switched-on criteria. */
    public void setEnabledCriteria(Set<String> enabledCriteria) {
        this.enabledCriteria = enabledCriteria;
    }

    /** Returns the weight per criterion name. */
    public Map<String, Double> getWeights() {
        return weights;
    }

    /** Replaces the weight per criterion name. */
    public void setWeights(Map<String, Double> weights) {
        this.weights = weights;
    }

    /** Returns the maximum number of results to show. */
    public int getMaxResults() {
        return maxResults;
    }

    /** Changes the maximum number of results to show. */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /** Returns the score below which a candidate is not shown. */
    public double getMinimumScore() {
        return minimumScore;
    }

    /** Changes the score below which a candidate is not shown. */
    public void setMinimumScore(double minimumScore) {
        this.minimumScore = minimumScore;
    }
}
