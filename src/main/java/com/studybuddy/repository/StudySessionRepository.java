package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.StudySession;

/** Stores and finds study sessions. The rest of the app only ever sees this interface. */
public interface StudySessionRepository {

    /** Saves a session, replacing any existing session with the same id. */
    void save(StudySession session);

    /** Returns the session with this id, or null if no such session. */
    StudySession findById(String id);

    /** Returns all sessions. */
    List<StudySession> findAll();
}
