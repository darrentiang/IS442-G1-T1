package com.studybuddy.service;

/** A Discord account that has just approved our app: its user id and the short-lived token to add it to the server. */
public class DiscordUser {

    private final String id;
    private final String accessToken;

    /**
     * Creates the result of a finished Discord login.
     *
     * @param id the Discord user id
     * @param accessToken the token Discord gave for this login; never stored
     */
    public DiscordUser(String id, String accessToken) {
        this.id = id;
        this.accessToken = accessToken;
    }

    /** Returns the Discord user id. */
    public String getId() {
        return id;
    }

    /** Returns the token Discord gave for this login. */
    public String getAccessToken() {
        return accessToken;
    }
}
