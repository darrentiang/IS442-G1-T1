package com.studybuddy.model;

/** The kind of real action an activity feed row was generated from. */
public enum ActivityType {
    /** A student joined the group. */
    MEMBER_JOINED,
    /** A student left or was removed from the group. */
    MEMBER_LEFT,
    /** A member proposed a group study session. */
    SESSION_PROPOSED,
    /** The proposer confirmed a group study session. */
    SESSION_CONFIRMED,
    /** The group passed to a new leader. */
    LEADERSHIP_TRANSFERRED
}
