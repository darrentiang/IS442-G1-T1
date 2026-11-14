package com.studybuddy.repository;

import com.studybuddy.model.SessionDetails;

import java.util.List;

/** Stores and finds the proposer and group of each study session. The rest of the app only ever sees this interface. */
public interface SessionDetailsRepository {

    /** Saves the details, replacing any existing details for the same session. */
    void save(SessionDetails details);

    /** Returns the details of this session, or null if there are none. */
    SessionDetails findBySession(String sessionId);

    /** Returns the details of every session planned for this group. */
    List<SessionDetails> findByGroup(String groupId);

    /** Removes the details of this session, if any. */
    void deleteBySession(String sessionId);
}
