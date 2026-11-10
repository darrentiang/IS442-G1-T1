package com.studybuddy.repository;

import org.springframework.stereotype.Repository;

import com.studybuddy.engine.advisor.AdvisorSettings;

/** Holds the BOSS Advisor settings in memory; they return to the defaults on restart. */
@Repository
public class InMemoryAdvisorSettingsRepository implements AdvisorSettingsRepository {

    private AdvisorSettings settings;

    /** Returns the current settings, or null if none have been saved yet. */
    @Override
    public AdvisorSettings get() {
        return settings;
    }

    /** Saves the settings, replacing the current ones. */
    @Override
    public void save(AdvisorSettings settings) {
        this.settings = settings;
    }
}
