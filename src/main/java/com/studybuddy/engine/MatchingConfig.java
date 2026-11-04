package com.studybuddy.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.studybuddy.engine.criteria.AvailabilityCriterion;
import com.studybuddy.engine.criteria.CourseCriterion;
import com.studybuddy.engine.criteria.GroupSizeCriterion;
import com.studybuddy.engine.criteria.IntensityCriterion;
import com.studybuddy.engine.criteria.SharedCoursesCriterion;
import com.studybuddy.engine.criteria.StudyGoalCriterion;
import com.studybuddy.engine.criteria.StudyModeCriterion;
import com.studybuddy.engine.criteria.YearCriterion;
import com.studybuddy.engine.strategy.BalancedStrategy;

/** The admin's settings: which strategy, which criteria on, what weights, and the tuning numbers. */
public class MatchingConfig {

    private static final int DEFAULT_MAX_RESULTS = 10;
    private static final double DEFAULT_MINIMUM_SCORE = 0.20;
    private static final int DEFAULT_TARGET_USABLE_HOURS = 4;
    private static final int DEFAULT_MIN_SESSION_HOURS = 2;
    private static final double DEFAULT_CONGESTION_STRENGTH = 0.05;

    private static final double WEIGHT_COURSE = 0.20;
    private static final double WEIGHT_AVAILABILITY = 0.25;
    private static final double WEIGHT_STUDY_MODE = 0.12;
    private static final double WEIGHT_STUDY_GOAL = 0.15;
    private static final double WEIGHT_GROUP_SIZE = 0.08;
    private static final double WEIGHT_YEAR = 0.07;
    private static final double WEIGHT_INTENSITY = 0.08;
    private static final double WEIGHT_SHARED_COURSES = 0.05;

    private String activeStrategyName;
    private Set<String> enabledCriteria = new HashSet<>();
    private Map<String, Double> weights = new HashMap<>();
    private int maxResults;
    private double minimumScore;
    private int targetUsableHours = DEFAULT_TARGET_USABLE_HOURS;
    private int minSessionHours = DEFAULT_MIN_SESSION_HOURS;
    private double congestionStrength = DEFAULT_CONGESTION_STRENGTH;

    /** Returns the settings the app starts with: balanced strategy, all 8 criteria on, the agreed weights. */
    public static MatchingConfig defaults() {
        MatchingConfig config = new MatchingConfig();
        config.setActiveStrategyName(BalancedStrategy.NAME);
        config.setMaxResults(DEFAULT_MAX_RESULTS);
        config.setMinimumScore(DEFAULT_MINIMUM_SCORE);
        Map<String, Double> defaultWeights = new HashMap<>();
        defaultWeights.put(CourseCriterion.NAME, WEIGHT_COURSE);
        defaultWeights.put(AvailabilityCriterion.NAME, WEIGHT_AVAILABILITY);
        defaultWeights.put(StudyModeCriterion.NAME, WEIGHT_STUDY_MODE);
        defaultWeights.put(StudyGoalCriterion.NAME, WEIGHT_STUDY_GOAL);
        defaultWeights.put(GroupSizeCriterion.NAME, WEIGHT_GROUP_SIZE);
        defaultWeights.put(YearCriterion.NAME, WEIGHT_YEAR);
        defaultWeights.put(IntensityCriterion.NAME, WEIGHT_INTENSITY);
        defaultWeights.put(SharedCoursesCriterion.NAME, WEIGHT_SHARED_COURSES);
        config.setWeights(defaultWeights);
        config.setEnabledCriteria(new HashSet<>(defaultWeights.keySet()));
        return config;
    }

    /** Returns the name of the strategy currently in use. */
    public String getActiveStrategyName() {
        return activeStrategyName;
    }

    /** Changes the strategy currently in use. */
    public void setActiveStrategyName(String activeStrategyName) {
        this.activeStrategyName = activeStrategyName;
    }

    /** Returns the names of the criteria that are switched on, as a read-only view. */
    public Set<String> getEnabledCriteria() {
        return Collections.unmodifiableSet(enabledCriteria);
    }

    /** Replaces the set of switched-on criteria with a copy of the given set. */
    public void setEnabledCriteria(Set<String> enabledCriteria) {
        this.enabledCriteria = new HashSet<>(enabledCriteria);
    }

    /** Returns the weight per criterion name, as a read-only view. */
    public Map<String, Double> getWeights() {
        return Collections.unmodifiableMap(weights);
    }

    /** Replaces the weight per criterion name with a copy of the given map. */
    public void setWeights(Map<String, Double> weights) {
        this.weights = new HashMap<>(weights);
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

    /** Returns the usable hours per week together that earn a full availability score. */
    public int getTargetUsableHours() {
        return targetUsableHours;
    }

    /** Changes the usable hours per week together that earn a full availability score. */
    public void setTargetUsableHours(int targetUsableHours) {
        this.targetUsableHours = targetUsableHours;
    }

    /** Returns the shortest shared block, in hours, that counts as a possible session. */
    public int getMinSessionHours() {
        return minSessionHours;
    }

    /** Changes the shortest shared block, in hours, that counts as a possible session. */
    public void setMinSessionHours(int minSessionHours) {
        this.minSessionHours = minSessionHours;
    }

    /** Returns how strongly pending requests lower a candidate's score. */
    public double getCongestionStrength() {
        return congestionStrength;
    }

    /** Changes how strongly pending requests lower a candidate's score. */
    public void setCongestionStrength(double congestionStrength) {
        this.congestionStrength = congestionStrength;
    }
}
