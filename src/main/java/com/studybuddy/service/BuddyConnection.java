package com.studybuddy.service;

import java.time.LocalDate;

import com.studybuddy.model.ProfileView;
import com.studybuddy.model.Student;

/** One row of a student's buddy list: the other student, seen with their contact number. */
public class BuddyConnection {

    private final String connectionId;
    private final Student buddy;
    private final LocalDate since;
    private final ProfileView view;

    /** Creates a row for one active connection, from the signed-in student's side. */
    public BuddyConnection(String connectionId, Student buddy, LocalDate since, ProfileView view) {
        this.connectionId = connectionId;
        this.buddy = buddy;
        this.since = since;
        this.view = view;
    }

    /** Returns the id of the connection, used to end it. */
    public String getConnectionId() {
        return connectionId;
    }

    /** Returns the other student in the connection. */
    public Student getBuddy() {
        return buddy;
    }

    /** Returns the date the two students connected. */
    public LocalDate getSince() {
        return since;
    }

    /** Returns the buddy's connected profile view, which includes the contact number. */
    public ProfileView getView() {
        return view;
    }
}
