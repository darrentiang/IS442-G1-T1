package com.studybuddy.repository;

import org.springframework.stereotype.Repository;

import com.studybuddy.engine.MatchingConfig;

/** Field-backed implementation: there is only ever one active configuration, so no map is needed. */
@Repository
public class InMemoryMatchingConfigRepository implements MatchingConfigRepository {

    private MatchingConfig config;

    /** Replaces the active configuration with this one. */
    @Override
    public void save(MatchingConfig config) {
        this.config = config;
    }

    /** Returns the active configuration, or null if none has been saved yet. */
    @Override
    public MatchingConfig get() {
        return config;
    }
}
