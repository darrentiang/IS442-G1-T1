package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.StudyGroup;

/** HashMap-backed implementation. */
@Repository
public class InMemoryStudyGroupRepository implements StudyGroupRepository {

    private final Map<String, StudyGroup> store = new HashMap<>();

    /** Saves a group, replacing any existing group with the same id. */
    @Override
    public void save(StudyGroup group) {
        store.put(group.getId(), group);
    }

    /** Returns the group with this id, or null if no such group. */
    @Override
    public StudyGroup findById(String id) {
        return store.get(id);
    }

    /** Returns all groups. */
    @Override
    public List<StudyGroup> findAll() {
        return new ArrayList<>(store.values());
    }
}
