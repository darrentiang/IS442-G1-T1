package com.studybuddy.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.DiscordChannel;

/** HashMap-backed implementation, keyed by group id. */
@Repository
public class InMemoryDiscordChannelRepository implements DiscordChannelRepository {

    private final Map<String, DiscordChannel> store = new HashMap<>();

    /** Saves a link, replacing any existing link for the same group. */
    @Override
    public void save(DiscordChannel channel) {
        store.put(channel.getGroupId(), channel);
    }

    /** Returns the link for this group, or null if the group has no Discord channel. */
    @Override
    public DiscordChannel findByGroup(String groupId) {
        return store.get(groupId);
    }

    /** Removes the link for this group, if there is one. */
    @Override
    public void deleteByGroup(String groupId) {
        store.remove(groupId);
    }
}
