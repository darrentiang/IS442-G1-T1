package com.studybuddy.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.DiscordAccount;

/** HashMap-backed implementation, keyed by student id. */
@Repository
public class InMemoryDiscordAccountRepository implements DiscordAccountRepository {

    private final Map<String, DiscordAccount> store = new HashMap<>();

    /** Saves a link, replacing any existing link for the same student. */
    @Override
    public void save(DiscordAccount account) {
        store.put(account.getStudentId(), account);
    }

    /** Returns the link for this student, or null if they have not connected Discord. */
    @Override
    public DiscordAccount findByStudent(String studentId) {
        return store.get(studentId);
    }
}
