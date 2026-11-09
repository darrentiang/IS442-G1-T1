package com.studybuddy.repository;

import com.studybuddy.model.DiscordAccount;

/** Stores which Discord account each student connected. */
public interface DiscordAccountRepository {

    /** Saves a link, replacing any existing link for the same student. */
    void save(DiscordAccount account);

    /** Returns the link for this student, or null if they have not connected Discord. */
    DiscordAccount findByStudent(String studentId);
}
