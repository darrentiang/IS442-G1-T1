package com.studybuddy.repository;

import com.studybuddy.engine.advisor.AdvisorSettings;

/** Stores the one current set of BOSS Advisor settings. */
public interface AdvisorSettingsRepository {

    /** Returns the current settings, or null if none have been saved yet. */
    AdvisorSettings get();

    /** Saves the settings, replacing the current ones. */
    void save(AdvisorSettings settings);
}
