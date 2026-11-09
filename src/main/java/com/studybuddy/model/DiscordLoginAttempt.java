package com.studybuddy.model;

import java.util.Objects;

/**
 * A Discord connect that has started but not finished: the student was sent to Discord with a
 * one-time state value, and it is checked when Discord sends them back, so nobody can fake the return.
 */
public class DiscordLoginAttempt {

    private final String state;
    private final String studentId;
    private final String groupId;

    /**
     * Creates the attempt.
     *
     * @param state the one-time value sent to Discord and expected back
     * @param studentId the student who started connecting
     * @param groupId the group page to return to afterwards
     */
    public DiscordLoginAttempt(String state, String studentId, String groupId) {
        this.state = state;
        this.studentId = studentId;
        this.groupId = groupId;
    }

    /** Returns the one-time value sent to Discord. */
    public String getState() {
        return state;
    }

    /** Returns the student who started connecting. */
    public String getStudentId() {
        return studentId;
    }

    /** Returns the group page to return to afterwards. */
    public String getGroupId() {
        return groupId;
    }

    /** Two attempts are equal when they have the same state value. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DiscordLoginAttempt)) {
            return false;
        }
        DiscordLoginAttempt that = (DiscordLoginAttempt) other;
        return Objects.equals(state, that.state);
    }

    /** Returns a hash based on the state value, matching equals. */
    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
