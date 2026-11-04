package com.studybuddy.seed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.repository.MatchingConfigRepository;

/**
 * Loads matching-config.csv. Columns: strategy,enabled_criteria,weights,max_results,
 * minimum_score,target_usable_hours,min_session_hours,congestion_strength. Weights look like
 * "course=0.20|availability=0.25". The file must hold exactly one configuration row.
 */
public class MatchingConfigSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "matching-config.csv";
    private static final int COLUMNS = 8;

    private final MatchingConfigRepository matchingConfigRepository;

    /** Creates the seeder with the repository it fills. */
    public MatchingConfigSeeder(MatchingConfigRepository matchingConfigRepository) {
        this.matchingConfigRepository = matchingConfigRepository;
    }

    /** Returns "matching-config.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves the one configuration row; returns 1. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        MatchingConfig config = null;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);

            if (config != null) {
                throw new SeedDataException(file.where(line) + ": only one configuration row is allowed");
            }
            config = new MatchingConfig();
            config.setActiveStrategyName(cells[0]);

            Set<String> enabled = new HashSet<>();
            for (String name : cells[1].split("\\|")) {
                enabled.add(name.trim());
            }
            config.setEnabledCriteria(enabled);

            Map<String, Double> weights = new HashMap<>();
            for (String pair : cells[2].split("\\|")) {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    throw new SeedDataException(file.where(line) + ": expected name=weight but found '" + pair + "'");
                }
                try {
                    weights.put(parts[0].trim(), Double.valueOf(parts[1].trim()));
                } catch (NumberFormatException e) {
                    throw new SeedDataException(file.where(line) + ": weight must be a number, found '"
                            + parts[1] + "'", e);
                }
            }
            config.setWeights(weights);

            config.setMaxResults(file.parseWholeNumber(cells[3], "max_results", line));
            config.setMinimumScore(file.parseNumber(cells[4], "minimum_score", line));
            config.setTargetUsableHours(file.parseWholeNumber(cells[5], "target_usable_hours", line));
            config.setMinSessionHours(file.parseWholeNumber(cells[6], "min_session_hours", line));
            config.setCongestionStrength(file.parseNumber(cells[7], "congestion_strength", line));
        }
        if (config == null) {
            throw new SeedDataException(file.getFileName() + ": expected one configuration row, found none");
        }
        matchingConfigRepository.save(config);
        return 1;
    }
}
