package com.studybuddy.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.GroupInvite;

/** HashMap-backed implementation, keyed both by group id and by token. */
@Repository
public class InMemoryGroupInviteRepository implements GroupInviteRepository {

    private final Map<String, GroupInvite> byGroup = new HashMap<>();
    private final Map<String, GroupInvite> byToken = new HashMap<>();

    /** Saves an invite, replacing any existing invite for the same group. */
    @Override
    public void save(GroupInvite invite) {
        deleteByGroup(invite.getGroupId());
        byGroup.put(invite.getGroupId(), invite);
        byToken.put(invite.getToken(), invite);
    }

    /** Returns the invite with this token, or null if no group uses it. */
    @Override
    public GroupInvite findByToken(String token) {
        return byToken.get(token);
    }

    /** Returns the invite for this group, or null if the group has none yet. */
    @Override
    public GroupInvite findByGroup(String groupId) {
        return byGroup.get(groupId);
    }

    /** Removes the invite for this group, if there is one. */
    @Override
    public void deleteByGroup(String groupId) {
        GroupInvite old = byGroup.remove(groupId);
        if (old != null) {
            byToken.remove(old.getToken());
        }
    }
}
