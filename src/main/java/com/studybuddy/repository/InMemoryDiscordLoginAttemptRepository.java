package com.studybuddy.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.DiscordLoginAttempt;

/** HashMap-backed implementation, keyed by state value. */
@Repository
public class InMemoryDiscordLoginAttemptRepository implements DiscordLoginAttemptRepository {

    private final Map<String, DiscordLoginAttempt> store = new HashMap<>();

    /** Saves an attempt. */
    @Override
    public void save(DiscordLoginAttempt attempt) {
        store.put(attempt.getState(), attempt);
    }

    /** Returns the attempt with this state value and removes it, so it can be used once; null if there is none. */
    @Override
    public DiscordLoginAttempt take(String state) {
        if (state == null) {
            return null;
        }
        return store.remove(state);
    }
}
