package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Connection;

/** HashMap-backed implementation. */
@Repository
public class InMemoryConnectionRepository implements ConnectionRepository {

    private final Map<String, Connection> store = new HashMap<>();

    /** Saves a connection, replacing any existing connection with the same id. */
    @Override
    public void save(Connection connection) {
        store.put(connection.getId(), connection);
    }

    /** Returns the connection with this id, or null if no such connection. */
    @Override
    public Connection findById(String id) {
        return store.get(id);
    }

    /** Returns all connections. */
    @Override
    public List<Connection> findAll() {
        return new ArrayList<>(store.values());
    }
}
