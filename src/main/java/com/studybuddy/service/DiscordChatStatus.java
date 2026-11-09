package com.studybuddy.service;

/** What a student sees in the group chat section of a group page. */
public enum DiscordChatStatus {

    /** Discord is not set up for this app, or the viewer is not a member: the section is hidden. */
    HIDDEN,
    /** The group has no Discord channel yet; the leader can set one up. */
    NO_CHANNEL,
    /** The group has a channel, but this student has not connected their Discord account. */
    NOT_CONNECTED,
    /** The student is connected and can open the group's channel. */
    READY
}
