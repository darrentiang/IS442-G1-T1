package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudySession;

/** HashMap-backed implementation. */
@Repository
public class InMemoryStudySessionRepository implements StudySessionRepository {

    private final Map<String, StudySession> store = new HashMap<>();
    private int nextNumber = 0;

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

    /** Returns an unused session id such as "SS001" or "SS002". */
    @Override
    public String nextId() {
        String id;
        do {
            nextNumber++;
            id = String.format("SS%03d", nextNumber);
        } while (store.containsKey(id));
        return id;
    }

    /** Returns every session this student is a participant of. */
    @Override
    public List<StudySession> findByParticipant(String studentId) {
        List<StudySession> result = new ArrayList<>();
        for (StudySession session : store.values()) {
            for (Student participant : session.getParticipants()) {
                if (participant.getId().equals(studentId)) {
                    result.add(session);
                    break;
                }
            }
        }
        return result;
    }

    /** Removes the session with this id, if any. */
    @Override
    public void delete(String id) {
        store.remove(id);
    }
}
