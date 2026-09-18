package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.StudySession;

/** HashMap-backed implementation. */
@Repository
public class InMemoryStudySessionRepository implements StudySessionRepository {

    private final Map<String, StudySession> store = new HashMap<>();

    /** Saves a session, replacing any existing session with the same id. */
    @Override
    public void save(StudySession session) {
        store.put(session.getId(), session);
    }

    /** Returns the session with this id, or null if no such session. */
    @Override
    public StudySession findById(String id) {
        return store.get(id);
    }

    /** Returns all sessions. */
    @Override
    public List<StudySession> findAll() {
        return new ArrayList<>(store.values());
    }
}
