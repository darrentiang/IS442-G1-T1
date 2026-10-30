package com.studybuddy.repository;

import com.studybuddy.engine.MatchingConfig;

/** Stores and finds the one active matching configuration. The rest of the app only ever sees this interface. */
public interface MatchingConfigRepository {

    /** Replaces the active configuration with this one. */
    void save(MatchingConfig config);

    /** Returns the active configuration, or null if none has been saved yet. */
    MatchingConfig get();
}
