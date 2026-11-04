package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Pass;

/** List-backed implementation: passes have no id to key a map on. */
@Repository
public class InMemoryPassRepository implements PassRepository {

    private final List<Pass> store = new ArrayList<>();

    /** Saves one pass. */
    @Override
    public void save(Pass pass) {
        store.add(pass);
    }

    /** Returns every pass this student has made, oldest first. */
    @Override
    public List<Pass> findBySeeker(String seekerId) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : store) {
            if (pass.getSeeker().getId().equals(seekerId)) {
                result.add(pass);
            }
        }
        return result;
    }

    /** Removes the seeker's pass on the candidate; returns true if there was one to remove. */
    @Override
    public boolean delete(String seekerId, String candidateId) {
        for (int i = 0; i < store.size(); i++) {
            Pass pass = store.get(i);
            if (pass.getSeeker().getId().equals(seekerId) && pass.getCandidate().getId().equals(candidateId)) {
                store.remove(i);
                return true;
            }
        }
        return false;
    }
}
