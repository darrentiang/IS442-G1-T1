package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Connection;
import com.studybuddy.model.Student;
import com.studybuddy.repository.ConnectionRepository;

/** Business rules for connections between students who accepted a buddy request. */
@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;

    /** Creates the service with the repository it reads and writes (constructor injection). */
    public ConnectionService(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    /** Returns this student's active connections as buddy rows, oldest first. */
    public List<BuddyConnection> findConnectionsFor(String studentId) {
        List<BuddyConnection> rows = new ArrayList<>();
        for (Connection connection : connectionRepository.findActiveFor(studentId)) {
            Student buddy = otherSide(connection, studentId);
            rows.add(new BuddyConnection(connection.getId(), buddy, connection.getSince(),
                    buddy.getProfile().toConnectedView()));
        }
        return rows;
    }

    /** Returns the student on the other side of the connection from this student. */
    private Student otherSide(Connection connection, String studentId) {
        if (connection.getA().getId().equals(studentId)) {
            return connection.getB();
        }
        return connection.getA();
    }
}
