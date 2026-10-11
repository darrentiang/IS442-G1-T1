package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.SystemAdministrator;

/** LinkedHashMap-backed implementation; keeps insertion order, so findAll() returns rows in CSV order. */
@Repository
public class InMemorySystemAdministratorRepository implements SystemAdministratorRepository {

    private final Map<String, SystemAdministrator> store = new LinkedHashMap<>();

    /** Saves an administrator, replacing any existing one with the same id. */
    @Override
    public void save(SystemAdministrator admin) {
        store.put(admin.getId(), admin);
    }

    /** Returns the administrator with this id, or null if no such administrator. */
    @Override
    public SystemAdministrator findById(String id) {
        return store.get(id);
    }

    /** Returns all administrators, in the order they were saved. */
    @Override
    public List<SystemAdministrator> findAll() {
        return new ArrayList<>(store.values());
    }
}
