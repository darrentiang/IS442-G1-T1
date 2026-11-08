package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.BusinessRuleException;
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

    /**
     * Ends an active connection that this student is part of. The connection is kept but marked
     * inactive, so neither side sees the other's contact number any more.
     *
     * @param connectionId the connection to end
     * @param studentId the signed-in student, who must be on either side of the connection
     * @return the other student, for the confirmation message
     * @throws BusinessRuleException if the connection does not exist, is not this student's, or has already ended
     */
    public Student endConnection(String connectionId, String studentId) {
        Connection connection = connectionRepository.findById(connectionId);
        if (connection == null || !isPartOf(connection, studentId)) {
            throw new BusinessRuleException("This connection isn't available");
        }
        if (!connection.isActive()) {
            throw new BusinessRuleException("This connection has already ended");
        }
        connection.setActive(false);
        connectionRepository.save(connection);
        return otherSide(connection, studentId);
    }

    /** Returns true if this student is on either side of the connection. */
    private boolean isPartOf(Connection connection, String studentId) {
        return connection.getA().getId().equals(studentId) || connection.getB().getId().equals(studentId);
    }

    /** Returns the student on the other side of the connection from this student. */
    private Student otherSide(Connection connection, String studentId) {
        if (connection.getA().getId().equals(studentId)) {
            return connection.getB();
        }
        return connection.getA();
    }
}
