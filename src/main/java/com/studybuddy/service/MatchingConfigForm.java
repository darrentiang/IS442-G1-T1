package com.studybuddy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Raw values from the matching configuration form, all as text; MatchingConfigService validates and converts them. */
public class MatchingConfigForm {

    private String activeStrategyName;
    private List<String> enabledCriteria = new ArrayList<>();
    private Map<String, String> weights = new HashMap<>();
    private String maxResults;
    private String minimumScore;

    /** Creates an empty form; the controller or service fills it in. */
    public MatchingConfigForm() {
    }

    /** Returns the chosen strategy name. */
    public String getActiveStrategyName() {
        return activeStrategyName;
    }

    /** Sets the chosen strategy name. */
    public void setActiveStrategyName(String activeStrategyName) {
        this.activeStrategyName = activeStrategyName;
    }

    /** Returns the names of the ticked criteria. */
    public List<String> getEnabledCriteria() {
        return enabledCriteria;
    }

    /** Sets the names of the ticked criteria. */
    public void setEnabledCriteria(List<String> enabledCriteria) {
        this.enabledCriteria = enabledCriteria;
    }

    /** Returns the weight as typed, per criterion name (not yet checked to be a number). */
    public Map<String, String> getWeights() {
        return weights;
    }

    /** Sets the weight as typed, per criterion name. */
    public void setWeights(Map<String, String> weights) {
        this.weights = weights;
    }

    /** Returns the max results as typed. */
    public String getMaxResults() {
        return maxResults;
    }

    /** Sets the max results as typed. */
    public void setMaxResults(String maxResults) {
        this.maxResults = maxResults;
    }

    /** Returns the minimum score as typed. */
    public String getMinimumScore() {
        return minimumScore;
    }

    /** Sets the minimum score as typed. */
    public void setMinimumScore(String minimumScore) {
        this.minimumScore = minimumScore;
    }
}
