package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.JoinRequest;

/** HashMap-backed implementation. */
@Repository
public class InMemoryJoinRequestRepository implements JoinRequestRepository {

    private final Map<String, JoinRequest> store = new HashMap<>();

    /** Saves a request, replacing any existing request with the same id. */
    @Override
    public void save(JoinRequest request) {
        store.put(request.getId(), request);
    }

    /** Returns the request with this id, or null if no such request. */
    @Override
    public JoinRequest findById(String id) {
        return store.get(id);
    }

    /** Returns all requests. */
    @Override
    public List<JoinRequest> findAll() {
        return new ArrayList<>(store.values());
    }
}
