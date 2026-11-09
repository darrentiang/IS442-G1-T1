package com.studybuddy.repository;

import com.studybuddy.model.DiscordLoginAttempt;

/** Stores Discord connects that have started but not finished. */
public interface DiscordLoginAttemptRepository {

    /** Saves an attempt. */
    void save(DiscordLoginAttempt attempt);

    /** Returns the attempt with this state value and removes it, so it can be used once; null if there is none. */
    DiscordLoginAttempt take(String state);
}
