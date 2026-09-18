package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.ActivityEvent;

/**
 * Stores and finds activity feed rows. Events have no id (FOUNDATION.md section 3),
 * so there is no findById — feeds are only ever appended to and read whole.
 */
public interface ActivityEventRepository {

    /** Saves one feed row. */
    void save(ActivityEvent event);

    /** Returns all feed rows, oldest first. */
    List<ActivityEvent> findAll();
}
