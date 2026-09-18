package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.MatchRequest;

/** HashMap-backed implementation. */
@Repository
public class InMemoryMatchRequestRepository implements MatchRequestRepository {

    private final Map<String, MatchRequest> store = new HashMap<>();

    /** Saves a request, replacing any existing request with the same id. */
    @Override
    public void save(MatchRequest request) {
        store.put(request.getId(), request);
    }

    /** Returns the request with this id, or null if no such request. */
    @Override
    public MatchRequest findById(String id) {
        return store.get(id);
    }

    /** Returns all requests. */
    @Override
    public List<MatchRequest> findAll() {
        return new ArrayList<>(store.values());
    }
}
