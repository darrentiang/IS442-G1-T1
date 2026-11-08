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
    private int nextNumber = 0;

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

    /** Returns an unused connection id such as "C001" or "C002". */
    @Override
    public String nextId() {
        String id;
        do {
            nextNumber++;
            id = String.format("C%03d", nextNumber);
        } while (store.containsKey(id));
        return id;
    }

    /** Returns the active connections where this student is either side. */
    @Override
    public List<Connection> findActiveFor(String studentId) {
        List<Connection> result = new ArrayList<>();
        for (Connection connection : store.values()) {
            boolean involved = connection.getA().getId().equals(studentId)
                    || connection.getB().getId().equals(studentId);
            if (connection.isActive() && involved) {
                result.add(connection);
            }
        }
        return result;
    }
}
