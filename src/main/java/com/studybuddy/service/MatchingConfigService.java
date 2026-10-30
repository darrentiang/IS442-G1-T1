package com.studybuddy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.repository.MatchingConfigRepository;

/** Business rules for the administrator's matching configuration: read, validate and update it. */
@Service
public class MatchingConfigService {

    private static final int MIN_RESULTS = 1;
    private static final int MAX_RESULTS = 50;
    private static final double MAX_WEIGHT = 1.0;

    // TODO: replace with the real names once the Student team's criterion and strategy classes exist.
    private static final String[] STRATEGY_NAMES = {"balanced", "availability-first", "course-first"};
    private static final String[] CRITERION_NAMES = {"course", "availability", "studyMode", "studyGoal", "groupSize"};

    private final MatchingConfigRepository configRepository;

    /** Creates the service with the repository it uses (constructor injection). */
    public MatchingConfigService(MatchingConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /** Returns the names of the strategies an administrator can choose from. */
    public List<String> getStrategyNames() {
        List<String> names = new ArrayList<>();
        for (String name : STRATEGY_NAMES) {
            names.add(name);
        }
        return names;
    }

    /** Returns the names of the criteria an administrator can switch on and weight. */
    public List<String> getCriterionNames() {
        List<String> names = new ArrayList<>();
        for (String name : CRITERION_NAMES) {
            names.add(name);
        }
        return names;
    }

    /** Returns the active configuration; if none was seeded, saves and returns a safe fallback. */
    public MatchingConfig getConfig() {
        MatchingConfig config = configRepository.get();
        if (config == null) {
            config = buildFallback();
            configRepository.save(config);
        }
        return config;
    }

    /** Turns the active configuration into form text, so the page can show its current values. */
    public MatchingConfigForm toForm(MatchingConfig config) {
        MatchingConfigForm form = new MatchingConfigForm();
        form.setActiveStrategyName(config.getActiveStrategyName());
        form.setEnabledCriteria(new ArrayList<>(config.getEnabledCriteria()));
        Map<String, String> weights = new HashMap<>();
        for (String name : CRITERION_NAMES) {
            Double weight = config.getWeights().get(name);
            weights.put(name, weight == null ? "0" : String.valueOf(weight));
        }
        form.setWeights(weights);
        form.setMaxResults(String.valueOf(config.getMaxResults()));
        form.setMinimumScore(String.valueOf(config.getMinimumScore()));
        return form;
    }

    /** Returns field name to message for every invalid field; empty when valid. */
    public Map<String, String> validate(MatchingConfigForm form) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (!isKnown(STRATEGY_NAMES, form.getActiveStrategyName())) {
            fieldErrors.put("activeStrategyName", "Choose a matching strategy");
        }

        boolean noneEnabled = form.getEnabledCriteria() == null || form.getEnabledCriteria().isEmpty();
        if (noneEnabled) {
            fieldErrors.put("criteria", "Turn on at least one criterion");
        }

        boolean weightsValid = true;
        double enabledTotal = 0;
        for (String name : CRITERION_NAMES) {
            Double weight = parseNumber(form.getWeights().get(name));
            if (weight == null || weight < 0 || weight > MAX_WEIGHT) {
                fieldErrors.put("weight_" + name, "Weight must be a number from 0 to 1");
                weightsValid = false;
            } else if (isEnabled(form, name)) {
                enabledTotal += weight;
            }
        }
        if (!noneEnabled && weightsValid && enabledTotal <= 0) {
            fieldErrors.put("criteria", "The weights of the turned-on criteria must add up to more than 0");
        }

        Integer maxResults = parseWholeNumber(form.getMaxResults());
        if (maxResults == null || maxResults < MIN_RESULTS || maxResults > MAX_RESULTS) {
            fieldErrors.put("maxResults", "Max results must be a whole number from " + MIN_RESULTS + " to " + MAX_RESULTS);
        }

        Double minimumScore = parseNumber(form.getMinimumScore());
        if (minimumScore == null || minimumScore < 0.0 || minimumScore > 1.0) {
            fieldErrors.put("minimumScore", "Minimum score must be a number from 0 to 1");
        }
        return fieldErrors;
    }

    /** Applies a form that has already passed validate() and saves it as the active configuration. */
    public void update(MatchingConfigForm form) {
        MatchingConfig config = new MatchingConfig();
        config.setActiveStrategyName(form.getActiveStrategyName());

        Set<String> enabled = new HashSet<>();
        Map<String, Double> weights = new HashMap<>();
        for (String name : CRITERION_NAMES) {
            if (isEnabled(form, name)) {
                enabled.add(name);
            }
            weights.put(name, parseNumber(form.getWeights().get(name)));
        }
        config.setEnabledCriteria(enabled);
        config.setWeights(weights);
        config.setMaxResults(parseWholeNumber(form.getMaxResults()));
        config.setMinimumScore(parseNumber(form.getMinimumScore()));
        configRepository.save(config);
    }

    /** Builds a safe default used only when no configuration was seeded. */
    private MatchingConfig buildFallback() {
        MatchingConfig config = new MatchingConfig();
        config.setActiveStrategyName(STRATEGY_NAMES[0]);
        Set<String> enabled = new HashSet<>();
        Map<String, Double> weights = new HashMap<>();
        for (String name : CRITERION_NAMES) {
            enabled.add(name);
            weights.put(name, 1.0);
        }
        config.setEnabledCriteria(enabled);
        config.setWeights(weights);
        config.setMaxResults(10);
        config.setMinimumScore(0.0);
        return config;
    }

    /** Returns whether the value is one of the allowed names. */
    private boolean isKnown(String[] allowed, String value) {
        for (String name : allowed) {
            if (name.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether this criterion is ticked in the form. */
    private boolean isEnabled(MatchingConfigForm form, String name) {
        return form.getEnabledCriteria() != null && form.getEnabledCriteria().contains(name);
    }

    /** Reads text as a finite number, or returns null if it is not one. */
    private Double parseNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Reads text as a whole number, or returns null if it is not one. */
    private Integer parseWholeNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
