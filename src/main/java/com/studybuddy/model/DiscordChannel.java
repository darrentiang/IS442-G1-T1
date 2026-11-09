package com.studybuddy.model;

import java.util.Objects;

/** Links a study group to its private Discord channel and the role that lets members see it. */
public class DiscordChannel {

    private final String groupId;
    private final String channelId;
    private final String roleId;

    /**
     * Creates the link.
     *
     * @param groupId the study group's id
     * @param channelId the Discord channel's id
     * @param roleId the Discord role that can see the channel
     */
    public DiscordChannel(String groupId, String channelId, String roleId) {
        this.groupId = groupId;
        this.channelId = channelId;
        this.roleId = roleId;
    }

    /** Returns the study group's id. */
    public String getGroupId() {
        return groupId;
    }

    /** Returns the Discord channel's id. */
    public String getChannelId() {
        return channelId;
    }

    /** Returns the Discord role that can see the channel. */
    public String getRoleId() {
        return roleId;
    }

    /** Two links are equal when they belong to the same group. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DiscordChannel)) {
            return false;
        }
        DiscordChannel that = (DiscordChannel) other;
        return Objects.equals(groupId, that.groupId);
    }

    /** Returns a hash based on the group id, matching equals. */
    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}
