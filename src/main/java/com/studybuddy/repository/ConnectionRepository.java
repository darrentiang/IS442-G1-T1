package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.Connection;

/** Stores and finds connections. The rest of the app only ever sees this interface. */
public interface ConnectionRepository {

    /** Saves a connection, replacing any existing connection with the same id. */
    void save(Connection connection);

    /** Returns the connection with this id, or null if no such connection. */
    Connection findById(String id);

    /** Returns all connections. */
    List<Connection> findAll();
}
