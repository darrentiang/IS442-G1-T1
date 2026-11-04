package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.MatchRequest;

/** Stores and finds match requests. The rest of the app only ever sees this interface. */
public interface MatchRequestRepository {

    /** Saves a request, replacing any existing request with the same id. */
    void save(MatchRequest request);

    /** Returns the request with this id, or null if no such request. */
    MatchRequest findById(String id);

    /** Returns all requests, in the order they were saved. */
    List<MatchRequest> findAll();

    /** Returns an unused request id such as "R001" or "R002". */
    String nextId();

    /** Returns every request (any status) sent by this student, in the order they were saved. */
    List<MatchRequest> findFrom(String studentId);

    /** Returns every request (any status) sent to this student, in the order they were saved. */
    List<MatchRequest> findTo(String studentId);
}
