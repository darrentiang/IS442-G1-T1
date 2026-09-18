package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.MatchRequest;

/** Stores and finds match requests. The rest of the app only ever sees this interface. */
public interface MatchRequestRepository {

    /** Saves a request, replacing any existing request with the same id. */
    void save(MatchRequest request);

    /** Returns the request with this id, or null if no such request. */
    MatchRequest findById(String id);

    /** Returns all requests. */
    List<MatchRequest> findAll();
}
