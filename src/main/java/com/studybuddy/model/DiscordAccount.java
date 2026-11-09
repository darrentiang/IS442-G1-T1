package com.studybuddy.model;

import java.util.Objects;

/** Links a student to the Discord account they connected, so the bot knows who to give access to. */
public class DiscordAccount {

    private final String studentId;
    private final String discordUserId;

    /**
     * Creates the link.
     *
     * @param studentId the student's id in this app
     * @param discordUserId the student's Discord user id
     */
    public DiscordAccount(String studentId, String discordUserId) {
        this.studentId = studentId;
        this.discordUserId = discordUserId;
    }

    /** Returns the student's id in this app. */
    public String getStudentId() {
        return studentId;
    }

    /** Returns the student's Discord user id. */
    public String getDiscordUserId() {
        return discordUserId;
    }

    /** Two links are equal when they belong to the same student. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DiscordAccount)) {
            return false;
        }
        DiscordAccount that = (DiscordAccount) other;
        return Objects.equals(studentId, that.studentId);
    }

    /** Returns a hash based on the student id, matching equals. */
    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
