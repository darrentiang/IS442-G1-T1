package com.studybuddy.repository;

import com.studybuddy.model.DiscordChannel;

/** Stores which Discord channel and role belong to each study group. */
public interface DiscordChannelRepository {

    /** Saves a link, replacing any existing link for the same group. */
    void save(DiscordChannel channel);

    /** Returns the link for this group, or null if the group has no Discord channel. */
    DiscordChannel findByGroup(String groupId);

    /** Removes the link for this group, if there is one. */
    void deleteByGroup(String groupId);
}
