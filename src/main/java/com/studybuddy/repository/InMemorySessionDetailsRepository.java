package com.studybuddy.repository;

import com.studybuddy.model.SessionDetails;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LinkedHashMap-backed implementation, keyed by session id; keeps insertion order. */
@Repository
public class InMemorySessionDetailsRepository implements SessionDetailsRepository {

    private final Map<String, SessionDetails> store = new LinkedHashMap<>();

    /** Saves the details, replacing any existing details for the same session. */
    @Override
    public void save(SessionDetails details) {
        store.put(details.getSessionId(), details);
    }

    /** Returns the details of this session, or null if there are none. */
    @Override
    public SessionDetails findBySession(String sessionId) {
        return store.get(sessionId);
    }

    /** Returns the details of every session planned for this group. */
    @Override
    public List<SessionDetails> findByGroup(String groupId) {
        List<SessionDetails> result = new ArrayList<>();
        for (SessionDetails details : store.values()) {
            if (groupId != null && groupId.equals(details.getGroupId())) {
                result.add(details);
            }
        }
        return result;
    }

    /** Removes the details of this session, if any. */
    @Override
    public void deleteBySession(String sessionId) {
        store.remove(sessionId);
    }
}
