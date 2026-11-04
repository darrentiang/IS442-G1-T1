package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.Pass;

/** Stores and finds passes (a student hiding a candidate from Find Buddies). */
public interface PassRepository {

    /** Saves one pass. */
    void save(Pass pass);

    /** Returns every pass this student has made, oldest first. */
    List<Pass> findBySeeker(String seekerId);

    /** Removes the seeker's pass on the candidate; returns true if there was one to remove. */
    boolean delete(String seekerId, String candidateId);
}
