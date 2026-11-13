package com.studybuddy.model;

import java.util.Objects;

/**
 * The parts of a study session that StudySession does not hold: who proposed it and, for a group
 * session, which group it was planned for. Kept in its own class so StudySession stays unchanged.
 */
public class SessionDetails {

    private final String sessionId;
    private final String proposerId;
    private final String groupId;

    /**
     * Creates the details for one session.
     *
     * @param sessionId the session these details belong to
     * @param proposerId the student who proposed the session
     * @param groupId the group the session was planned for, or null for a session between two buddies
     */
    public SessionDetails(String sessionId, String proposerId, String groupId) {
        this.sessionId = sessionId;
        this.proposerId = proposerId;
        this.groupId = groupId;
    }

    /** Returns the id of the session these details belong to. */
    public String getSessionId() {
        return sessionId;
    }

    /** Returns the id of the student who proposed the session. */
    public String getProposerId() {
        return proposerId;
    }

    /** Returns the id of the group the session was planned for, or null for a buddy session. */
    public String getGroupId() {
        return groupId;
    }

    /** Returns true if the session was planned for a study group rather than two buddies. */
    public boolean isGroupSession() {
        return groupId != null;
    }

    /** Returns a short description of these details for logs and debugging. */
    @Override
    public String toString() {
        return "SessionDetails[" + sessionId + " by " + proposerId + (groupId == null ? "" : " for " + groupId) + "]";
    }

    /** Two details are equal when they belong to the same session. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SessionDetails)) {
            return false;
        }
        SessionDetails that = (SessionDetails) other;
        return Objects.equals(sessionId, that.sessionId);
    }

    /** Returns a hash code from the session id, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}
