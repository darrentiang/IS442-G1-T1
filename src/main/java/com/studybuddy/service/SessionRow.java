package com.studybuddy.service;

import com.studybuddy.model.Rsvp;
import com.studybuddy.model.SessionStatus;
import com.studybuddy.model.StudySession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One study session as a page shows it to one student: when and where, who is going, and which
 * buttons that student may press. Built by SessionService, so the page needs no rules of its own.
 */
public class SessionRow {

    private final StudySession session;
    private final String when;
    private final String withLabel;
    private final String proposerName;
    private final List<String> going;
    private final List<String> cant;
    private final List<String> waiting;
    private final Rsvp myAnswer;
    private final boolean participant;
    private final boolean proposer;
    private final boolean confirmable;

    /**
     * Creates a row; the lists are copied.
     *
     * @param session the session shown
     * @param when the day and time as text, such as "WED 19:00–21:00"
     * @param withLabel who the session is with, such as "Ben Tan" or "IS442 Exam Crew"
     * @param proposerName the name of the student who proposed it
     * @param going names of participants who answered Going
     * @param cant names of participants who answered Can't
     * @param waiting names of participants who have not answered
     * @param myAnswer the viewing student's answer, or null if they have not answered or are not a participant
     * @param participant true if the viewing student is one of the participants
     * @param proposer true if the viewing student proposed the session
     * @param confirmable true if the viewing student may confirm the session now
     */
    public SessionRow(StudySession session, String when, String withLabel, String proposerName,
            List<String> going, List<String> cant, List<String> waiting, Rsvp myAnswer,
            boolean participant, boolean proposer, boolean confirmable) {
        this.session = session;
        this.when = when;
        this.withLabel = withLabel;
        this.proposerName = proposerName;
        this.going = new ArrayList<>(going);
        this.cant = new ArrayList<>(cant);
        this.waiting = new ArrayList<>(waiting);
        this.myAnswer = myAnswer;
        this.participant = participant;
        this.proposer = proposer;
        this.confirmable = confirmable;
    }

    /** Returns the session shown in this row. */
    public StudySession getSession() {
        return session;
    }

    /** Returns the session's status. */
    public SessionStatus getStatus() {
        return session.getStatus();
    }

    /** Returns the day and time as text, such as "WED 19:00–21:00". */
    public String getWhen() {
        return when;
    }

    /** Returns who the session is with, from the viewing student's side. */
    public String getWithLabel() {
        return withLabel;
    }

    /** Returns the name of the student who proposed the session. */
    public String getProposerName() {
        return proposerName;
    }

    /** Returns the names of participants who are going, as a read-only list. */
    public List<String> getGoing() {
        return Collections.unmodifiableList(going);
    }

    /** Returns the names of participants who can't make it, as a read-only list. */
    public List<String> getCant() {
        return Collections.unmodifiableList(cant);
    }

    /** Returns the names of participants who have not answered yet, as a read-only list. */
    public List<String> getWaiting() {
        return Collections.unmodifiableList(waiting);
    }

    /** Returns the viewing student's answer, or null if there is none. */
    public Rsvp getMyAnswer() {
        return myAnswer;
    }

    /** Returns true if the viewing student is one of the participants. */
    public boolean isParticipant() {
        return participant;
    }

    /** Returns true if the viewing student proposed the session. */
    public boolean isProposer() {
        return proposer;
    }

    /** Returns true if the viewing student may confirm the session now. */
    public boolean isConfirmable() {
        return confirmable;
    }

    /** Returns true while the session has not happened yet (proposed or confirmed). */
    public boolean isUpcoming() {
        return session.getStatus() != SessionStatus.PAST;
    }
}
