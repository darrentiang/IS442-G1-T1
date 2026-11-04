package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.MatchRequest;

/** LinkedHashMap-backed implementation; keeps insertion order, so lists come back in seed order. */
@Repository
public class InMemoryMatchRequestRepository implements MatchRequestRepository {

    private final Map<String, MatchRequest> store = new LinkedHashMap<>();
    private int nextNumber = 0;

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

    /** Returns all requests, in the order they were saved. */
    @Override
    public List<MatchRequest> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Returns an unused request id such as "R001", counting up and skipping ids already in the store. */
    @Override
    public String nextId() {
        String id;
        do {
            nextNumber++;
            id = String.format("R%03d", nextNumber);
        } while (store.containsKey(id));
        return id;
    }

    /** Returns every request (any status) sent by this student, in the order they were saved. */
    @Override
    public List<MatchRequest> findFrom(String studentId) {
        List<MatchRequest> result = new ArrayList<>();
        for (MatchRequest request : store.values()) {
            if (request.getFrom().getId().equals(studentId)) {
                result.add(request);
            }
        }
        return result;
    }

    /** Returns every request (any status) sent to this student, in the order they were saved. */
    @Override
    public List<MatchRequest> findTo(String studentId) {
        List<MatchRequest> result = new ArrayList<>();
        for (MatchRequest request : store.values()) {
            if (request.getTo().getId().equals(studentId)) {
                result.add(request);
            }
        }
        return result;
    }
}
