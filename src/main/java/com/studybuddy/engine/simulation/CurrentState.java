package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.studybuddy.model.Connection;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.Student;

/** A copy of what the app holds right now (students, buddy requests, connections) that every simulation starts from. */
public class CurrentState {

    private final List<Student> students;
    private final List<MatchRequest> requests;
    private final List<Connection> connections;

    /**
     * Creates the state from copies of the given lists.
     *
     * @param students every student
     * @param requests every buddy request
     * @param connections every connection
     */
    public CurrentState(List<Student> students, List<MatchRequest> requests, List<Connection> connections) {
        this.students = new ArrayList<>(students);
        this.requests = new ArrayList<>(requests);
        this.connections = new ArrayList<>(connections);
    }

    /** Returns every student, as a read-only view. */
    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    /** Returns every buddy request, as a read-only view. */
    public List<MatchRequest> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    /** Returns every connection, as a read-only view. */
    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }
}
