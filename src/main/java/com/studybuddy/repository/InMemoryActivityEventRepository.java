package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.ActivityEvent;

/** List-backed implementation: events have no id to key a map on, and order matters. */
@Repository
public class InMemoryActivityEventRepository implements ActivityEventRepository {

    private final List<ActivityEvent> store = new ArrayList<>();

    /** Saves one feed row. */
    @Override
    public void save(ActivityEvent event) {
        store.add(event);
    }

    /** Returns all feed rows, oldest first. */
    @Override
    public List<ActivityEvent> findAll() {
        return new ArrayList<>(store);
    }

    /** Returns this group's feed rows, oldest first. */
    @Override
    public List<ActivityEvent> findByGroup(String groupId) {
        List<ActivityEvent> result = new ArrayList<>();
        for (ActivityEvent event : store) {
            if (event.getGroup() != null && event.getGroup().getId().equals(groupId)) {
                result.add(event);
            }
        }
        return result;
    }
}
