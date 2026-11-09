package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.studybuddy.model.Student;

/**
 * One student's state during a simulated term: who is waiting in their inbox, who they have
 * already been in contact with, and their connections. The real Student is only read, never changed.
 */
public class SimStudent {

    private final Student student;
    private final List<SimRequest> inbox = new ArrayList<>();
    private final Set<String> contactedIds = new HashSet<>();
    private final List<SimConnection> connections = new ArrayList<>();

    /** Creates the simulated state for a real student, starting empty. */
    public SimStudent(Student student) {
        this.student = student;
    }

    /** Returns the real student this state belongs to. */
    public Student getStudent() {
        return student;
    }

    /** Returns the student's id. */
    public String getId() {
        return student.getId();
    }

    /** Returns the requests waiting for an answer, as a read-only view. */
    public List<SimRequest> getInbox() {
        return Collections.unmodifiableList(inbox);
    }

    /** Returns how many requests are waiting for an answer. */
    public int getInboxSize() {
        return inbox.size();
    }

    /** Adds a request to the inbox and remembers the sender as contacted. */
    public void receive(SimRequest request) {
        inbox.add(request);
        contactedIds.add(request.getFromId());
    }

    /** Removes a request from the inbox once it is answered or has expired. */
    public void removeFromInbox(SimRequest request) {
        inbox.remove(request);
    }

    /** Remembers a student this one has sent a request to, so it is not sent twice. */
    public void markContacted(String otherId) {
        contactedIds.add(otherId);
    }

    /** Returns the ids of everyone this student has sent to, received from or connected with, as a read-only view. */
    public Set<String> getContactedIds() {
        return Collections.unmodifiableSet(contactedIds);
    }

    /** Adds a connection and remembers the other student as contacted. */
    public void addConnection(SimConnection connection, String otherId) {
        connections.add(connection);
        contactedIds.add(otherId);
    }

    /** Returns true while the student has no buddy at all, so is still sending requests. */
    public boolean isSearching() {
        return connections.isEmpty();
    }

    /** Returns true if at least one connection is a buddy this student shares free time with. */
    public boolean hasBuddyToMeet() {
        for (SimConnection connection : connections) {
            if (connection.canMeet()) {
                return true;
            }
        }
        return false;
    }
}
