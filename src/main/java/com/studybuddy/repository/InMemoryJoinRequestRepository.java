package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.JoinRequest;

/** LinkedHashMap-backed implementation; keeps insertion order, so lists come back in seed order. */
@Repository
public class InMemoryJoinRequestRepository implements JoinRequestRepository {

    private final Map<String, JoinRequest> store = new LinkedHashMap<>();
    private int nextNumber = 0;

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

    /** Returns all requests, in the order they were saved. */
    @Override
    public List<JoinRequest> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Returns an unused request id such as "J001", counting up and skipping ids already in the store. */
    @Override
    public String nextId() {
        String id;
        do {
            nextNumber++;
            id = String.format("J%03d", nextNumber);
        } while (store.containsKey(id));
        return id;
    }

    /** Returns every request (any status) made to this group, in the order they were saved. */
    @Override
    public List<JoinRequest> findByGroup(String groupId) {
        List<JoinRequest> result = new ArrayList<>();
        for (JoinRequest request : store.values()) {
            if (request.getGroup().getId().equals(groupId)) {
                result.add(request);
            }
        }
        return result;
    }

    /** Returns every request (any status) made by this student, in the order they were saved. */
    @Override
    public List<JoinRequest> findByStudent(String studentId) {
        List<JoinRequest> result = new ArrayList<>();
        for (JoinRequest request : store.values()) {
            if (request.getStudent().getId().equals(studentId)) {
                result.add(request);
            }
        }
        return result;
    }
}
