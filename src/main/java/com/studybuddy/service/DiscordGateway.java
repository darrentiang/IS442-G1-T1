package com.studybuddy.service;

import com.studybuddy.model.DiscordChannel;

/**
 * The calls this app makes to Discord, kept behind an interface so the rules in DiscordService do
 * not depend on how Discord is reached. Every method except isConfigured, loginUrl and channelUrl
 * talks to Discord and throws DiscordException if that fails.
 */
public interface DiscordGateway {

    /** Returns true if every Discord setting (bot token, client id and secret, server id, redirect) is present. */
    boolean isConfigured();

    /** Returns the Discord page that asks the student to approve our app, carrying the one-time state value. */
    String loginUrl(String state);

    /** Swaps the code Discord sent back for the student's Discord user id and a short-lived token. */
    DiscordUser exchangeCode(String code);

    /** Adds the Discord user to our server; does nothing if they are already in it. */
    void addToServer(DiscordUser user);

    /**
     * Creates a role and a channel that only that role (and the bot) can see.
     *
     * @param groupId the study group the channel is for
     * @param name the channel name
     * @return the link between the group, the channel and the role
     */
    DiscordChannel createPrivateChannel(String groupId, String name);

    /** Deletes the channel and its role. */
    void deleteChannel(DiscordChannel channel);

    /** Gives a server member the role, so they can see its channel. */
    void grantRole(String discordUserId, String roleId);

    /** Takes the role away from a server member, so its channel disappears for them. */
    void revokeRole(String discordUserId, String roleId);

    /** Returns the web address that opens the channel in Discord. */
    String channelUrl(String channelId);
}
