package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.JoinRequest;

/** Stores and finds join requests. The rest of the app only ever sees this interface. */
public interface JoinRequestRepository {

    /** Saves a request, replacing any existing request with the same id. */
    void save(JoinRequest request);

    /** Returns the request with this id, or null if no such request. */
    JoinRequest findById(String id);

    /** Returns all requests, in the order they were saved. */
    List<JoinRequest> findAll();

    /** Returns an unused request id such as "J001" or "J002" */
    String nextId();

    /** Returns every request (any status) made to this group, in the order they were saved. */
    List<JoinRequest> findByGroup(String groupId);

    /** Returns every request (any status) made by this student, in the order they were saved. */
    List<JoinRequest> findByStudent(String studentId);
}
