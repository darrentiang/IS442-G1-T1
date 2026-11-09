package com.studybuddy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.repository.MatchingConfigRepository;

/**
 * Business rules for the administrator's matching configuration: read, validate and update it.
 * The criterion and strategy names come from the matching engine (through MatchingService), so
 * adding a criterion or strategy to the engine makes it appear on this page with no change here.
 */
@Service
public class MatchingConfigService {

    // Bounds of the engine's 0-to-1 scoring scale; not settings, so they stay in code.
    private static final double MAX_WEIGHT = 1.0;
    private static final double MAX_CONGESTION_STRENGTH = 1.0;
    private static final int MIN_TARGET_USABLE_HOURS = 1;
    private static final int MAX_TARGET_USABLE_HOURS = 15;

    private final MatchingConfigRepository configRepository;
    private final MatchingService matchingService;
    private final int minResultsLimit;
    private final int maxResultsLimit;
    private final int minSessionHoursLimit;
    private final int maxSessionHoursLimit;

    /**
     * Creates the service with the repository it saves to, the service that knows the engine, and the
     * ranges the admin may choose from application.properties (constructor injection).
     */
    public MatchingConfigService(MatchingConfigRepository configRepository, MatchingService matchingService,
            Environment environment) {
        this.configRepository = configRepository;
        this.matchingService = matchingService;
        this.minResultsLimit = environment.getRequiredProperty("studybuddy.matching.min-results", Integer.class);
        this.maxResultsLimit = environment.getRequiredProperty("studybuddy.matching.max-results", Integer.class);
        this.minSessionHoursLimit = environment.getRequiredProperty("studybuddy.matching.min-session-hours",
                Integer.class);
        this.maxSessionHoursLimit = environment.getRequiredProperty("studybuddy.matching.max-session-hours",
                Integer.class);
    }

    /** Returns the names of the strategies an administrator can choose from, in engine order. */
    public List<String> getStrategyNames() {
        return matchingService.getStrategyNames();
    }

    /** Returns the names of the criteria an administrator can switch on and weight, in engine order. */
    public List<String> getCriterionNames() {
        return matchingService.getCriterionNames();
    }

    /** Returns strategy name to display name, e.g. "availability-first" to "Availability first", in engine order. */
    public Map<String, String> getStrategyLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String name : getStrategyNames()) {
            labels.put(name, matchingService.getStrategyLabel(name));
        }
        return labels;
    }

    /** Returns strategy name to its one-sentence description, in engine order, for the config page legend. */
    public Map<String, String> getStrategyDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        for (String name : getStrategyNames()) {
            descriptions.put(name, matchingService.getStrategyDescription(name));
        }
        return descriptions;
    }

    /** Returns criterion name to display name, e.g. "studyMode" to "Study mode", in engine order. */
    public Map<String, String> getCriterionLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String name : getCriterionNames()) {
            labels.put(name, matchingService.getCriterionLabel(name));
        }
        return labels;
    }

    /** Returns the active configuration (the one MatchingService ranks with). */
    public MatchingConfig getConfig() {
        return matchingService.getConfig();
    }

    /** Turns the active configuration into form text, so the page can show its current values. */
    public MatchingConfigForm toForm(MatchingConfig config) {
        MatchingConfigForm form = new MatchingConfigForm();
        form.setActiveStrategyName(config.getActiveStrategyName());
        form.setEnabledCriteria(new ArrayList<>(config.getEnabledCriteria()));
        Map<String, String> weights = new HashMap<>();
        for (String name : getCriterionNames()) {
            Double weight = config.getWeights().get(name);
            weights.put(name, weight == null ? "0" : String.valueOf(weight));
        }
        form.setWeights(weights);
        form.setMaxResults(String.valueOf(config.getMaxResults()));
        form.setMinimumScore(String.valueOf(config.getMinimumScore()));
        form.setTargetUsableHours(String.valueOf(config.getTargetUsableHours()));
        form.setMinSessionHours(String.valueOf(config.getMinSessionHours()));
        form.setCongestionStrength(String.valueOf(config.getCongestionStrength()));
        return form;
    }

    /**
     * Returns field name to message for every invalid field; empty when valid. The strategy and
     * every ticked criterion must be one the engine knows.
     */
    public Map<String, String> validate(MatchingConfigForm form) {
        Map<String, String> fieldErrors = new HashMap<>();
        List<String> criterionNames = getCriterionNames();

        if (!getStrategyNames().contains(form.getActiveStrategyName())) {
            fieldErrors.put("activeStrategyName", "Choose a matching strategy");
        }

        boolean noneEnabled = form.getEnabledCriteria() == null || form.getEnabledCriteria().isEmpty();
        if (noneEnabled) {
            fieldErrors.put("criteria", "Turn on at least one criterion");
        } else {
            for (String name : form.getEnabledCriteria()) {
                if (!criterionNames.contains(name)) {
                    fieldErrors.put("criteria", "Unknown criterion '" + name + "'");
                }
            }
        }

        boolean weightsValid = true;
        double enabledTotal = 0;
        for (String name : criterionNames) {
            Double weight = parseNumber(form.getWeights().get(name));
            if (weight == null || weight < 0 || weight > MAX_WEIGHT) {
                fieldErrors.put("weight_" + name, "Weight must be a number from 0 to 1");
                weightsValid = false;
            } else if (isEnabled(form, name)) {
                enabledTotal += weight;
            }
        }
        if (!noneEnabled && weightsValid && enabledTotal <= 0 && !fieldErrors.containsKey("criteria")) {
            fieldErrors.put("criteria", "The weights of the turned-on criteria must add up to more than 0");
        }

        Integer maxResults = parseWholeNumber(form.getMaxResults());
        if (maxResults == null || maxResults < minResultsLimit || maxResults > maxResultsLimit) {
            fieldErrors.put("maxResults", "Max results must be a whole number from " + minResultsLimit + " to " + maxResultsLimit);
        }

        Double minimumScore = parseNumber(form.getMinimumScore());
        if (minimumScore == null || minimumScore < 0.0 || minimumScore > 1.0) {
            fieldErrors.put("minimumScore", "Minimum score must be a number from 0 to 1");
        }

        Integer targetUsableHours = parseWholeNumber(form.getTargetUsableHours());
        if (targetUsableHours == null || targetUsableHours < MIN_TARGET_USABLE_HOURS
                || targetUsableHours > MAX_TARGET_USABLE_HOURS) {
            fieldErrors.put("targetUsableHours", "Target usable hours must be a whole number from "
                    + MIN_TARGET_USABLE_HOURS + " to " + MAX_TARGET_USABLE_HOURS);
        }

        Integer minSessionHours = parseWholeNumber(form.getMinSessionHours());
        if (minSessionHours == null || minSessionHours < minSessionHoursLimit || minSessionHours > maxSessionHoursLimit) {
            fieldErrors.put("minSessionHours", "Minimum session hours must be a whole number from "
                    + minSessionHoursLimit + " to " + maxSessionHoursLimit);
        }

        Double congestionStrength = parseNumber(form.getCongestionStrength());
        if (congestionStrength == null || congestionStrength < 0.0 || congestionStrength > MAX_CONGESTION_STRENGTH) {
            fieldErrors.put("congestionStrength", "Congestion strength must be a number from 0 to 1");
        }
        return fieldErrors;
    }

    /** Applies a form that has already passed validate() and saves it as the active configuration. */
    public void update(MatchingConfigForm form) {
        configRepository.save(fromForm(form));
    }

    /** Turns a form that has already passed validate() into a configuration, without saving it. */
    public MatchingConfig fromForm(MatchingConfigForm form) {
        MatchingConfig config = new MatchingConfig();
        config.setActiveStrategyName(form.getActiveStrategyName());

        Set<String> enabled = new HashSet<>();
        Map<String, Double> weights = new HashMap<>();
        for (String name : getCriterionNames()) {
            if (isEnabled(form, name)) {
                enabled.add(name);
            }
            weights.put(name, parseNumber(form.getWeights().get(name)));
        }
        config.setEnabledCriteria(enabled);
        config.setWeights(weights);
        config.setMaxResults(parseWholeNumber(form.getMaxResults()));
        config.setMinimumScore(parseNumber(form.getMinimumScore()));
        config.setTargetUsableHours(parseWholeNumber(form.getTargetUsableHours()));
        config.setMinSessionHours(parseWholeNumber(form.getMinSessionHours()));
        config.setCongestionStrength(parseNumber(form.getCongestionStrength()));
        return config;
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
