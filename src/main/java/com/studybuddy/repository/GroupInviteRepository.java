package com.studybuddy.repository;

import com.studybuddy.model.GroupInvite;

/** Stores the invite link token of each private study group. */
public interface GroupInviteRepository {

    /** Saves an invite, replacing any existing invite for the same group. */
    void save(GroupInvite invite);

    /** Returns the invite with this token, or null if no group uses it. */
    GroupInvite findByToken(String token);

    /** Returns the invite for this group, or null if the group has none yet. */
    GroupInvite findByGroup(String groupId);

    /** Removes the invite for this group, if there is one. */
    void deleteByGroup(String groupId);
}
