package com.studybuddy.model;

import java.util.Objects;

/** Links a private study group to the secret token in its invite link; StudyGroup knows nothing about it. */
public class GroupInvite {

    private final String groupId;
    private final String token;

    /**
     * Creates the link.
     *
     * @param groupId the study group's id
     * @param token the secret token used in the invite link
     */
    public GroupInvite(String groupId, String token) {
        this.groupId = groupId;
        this.token = token;
    }

    /** Returns the study group's id. */
    public String getGroupId() {
        return groupId;
    }

    /** Returns the secret token used in the invite link. */
    public String getToken() {
        return token;
    }

    /** Returns a short description of this invite for logs and debugging. */
    @Override
    public String toString() {
        return "GroupInvite[" + groupId + "]";
    }

    /** Two invites are equal when they have the same token. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GroupInvite)) {
            return false;
        }
        GroupInvite that = (GroupInvite) other;
        return Objects.equals(token, that.token);
    }

    /** Returns a hash code from the token, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
