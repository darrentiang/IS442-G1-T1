package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.JoinRequest;

/** Stores and finds join requests. The rest of the app only ever sees this interface. */
public interface JoinRequestRepository {

    /** Saves a request, replacing any existing request with the same id. */
    void save(JoinRequest request);

    /** Returns the request with this id, or null if no such request. */
    JoinRequest findById(String id);

    /** Returns all requests. */
    List<JoinRequest> findAll();
}
