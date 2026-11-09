package com.studybuddy.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studybuddy.model.DiscordChannel;

/**
 * Reaches Discord over its web API with Java's built-in HTTP client. Settings come from the
 * DISCORD_* values in the git-ignored .env file or environment variables; see .env.example.
 */
@Service
public class HttpDiscordGateway implements DiscordGateway {

    private static final String API = "https://discord.com/api/v10";
    private static final String AUTHORIZE_PAGE = "https://discord.com/oauth2/authorize";
    private static final String USER_AGENT = "DiscordBot (https://github.com/darrentiang/IS442-G1-T1, 1.0)";
    /** Discord's "View Channel" permission bit. */
    private static final String VIEW_CHANNEL = "1024";
    private static final int TEXT_CHANNEL = 0;
    private static final int ROLE_OVERWRITE = 0;
    private static final int MEMBER_OVERWRITE = 1;
    private static final int MAX_CHANNEL_NAME = 90;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String botToken;
    private final String clientId;
    private final String clientSecret;
    private final String guildId;
    private final String redirectUri;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final ObjectMapper json = new ObjectMapper();
    private String botUserId;

    /** Reads the Discord settings once at startup; any missing setting turns the feature off (constructor injection). */
    public HttpDiscordGateway(Environment environment) {
        this.botToken = setting(environment, "DISCORD_BOT_TOKEN");
        this.clientId = setting(environment, "DISCORD_CLIENT_ID");
        this.clientSecret = setting(environment, "DISCORD_CLIENT_SECRET");
        this.guildId = setting(environment, "DISCORD_GUILD_ID");
        this.redirectUri = setting(environment, "DISCORD_REDIRECT_URI");
    }

    /** Returns true if every Discord setting is present. */
    @Override
    public boolean isConfigured() {
        return !botToken.isEmpty() && !clientId.isEmpty() && !clientSecret.isEmpty()
                && !guildId.isEmpty() && !redirectUri.isEmpty();
    }

    /** Returns the Discord page that asks the student to approve our app, carrying the one-time state value. */
    @Override
    public String loginUrl(String state) {
        return AUTHORIZE_PAGE + "?client_id=" + encode(clientId)
                + "&response_type=code"
                + "&redirect_uri=" + encode(redirectUri)
                + "&scope=identify%20guilds.join"
                + "&state=" + encode(state);
    }

    /** Swaps the code Discord sent back for the student's Discord user id and a short-lived token. */
    @Override
    public DiscordUser exchangeCode(String code) {
        String form = "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirectUri);
        HttpRequest tokenRequest = HttpRequest.newBuilder(URI.create(API + "/oauth2/token"))
                .timeout(TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        String accessToken = send(tokenRequest).path("access_token").asText("");
        if (accessToken.isEmpty()) {
            throw new DiscordException("Discord did not return an access token");
        }
        HttpRequest meRequest = HttpRequest.newBuilder(URI.create(API + "/users/@me"))
                .timeout(TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        String userId = send(meRequest).path("id").asText("");
        if (userId.isEmpty()) {
            throw new DiscordException("Discord did not return a user id");
        }
        return new DiscordUser(userId, accessToken);
    }

    /** Adds the Discord user to our server; Discord does nothing if they are already in it. */
    @Override
    public void addToServer(DiscordUser user) {
        ObjectNode body = json.createObjectNode();
        body.put("access_token", user.getAccessToken());
        send(botRequest("/guilds/" + guildId + "/members/" + user.getId(), "PUT", body));
    }

    /** Creates a role and a channel that only that role and the bot can see. */
    @Override
    public DiscordChannel createPrivateChannel(String groupId, String name) {
        String channelName = toChannelName(name);
        ObjectNode roleBody = json.createObjectNode();
        roleBody.put("name", channelName);
        String roleId = send(botRequest("/guilds/" + guildId + "/roles", "POST", roleBody)).path("id").asText();

        ObjectNode channelBody = json.createObjectNode();
        channelBody.put("name", channelName);
        channelBody.put("type", TEXT_CHANNEL);
        channelBody.put("topic", "Study Buddy group chat for " + name);
        ArrayNode overwrites = channelBody.putArray("permission_overwrites");
        // The server's id is also the id of its @everyone role: hide the channel from everyone...
        overwrites.add(overwrite(guildId, ROLE_OVERWRITE, null, VIEW_CHANNEL));
        // ...then let the group's role and the bot itself see it.
        overwrites.add(overwrite(roleId, ROLE_OVERWRITE, VIEW_CHANNEL, null));
        overwrites.add(overwrite(botUserId(), MEMBER_OVERWRITE, VIEW_CHANNEL, null));
        String channelId;
        try {
            channelId = send(botRequest("/guilds/" + guildId + "/channels", "POST", channelBody)).path("id").asText();
        } catch (DiscordException exception) {
            deleteQuietly("/guilds/" + guildId + "/roles/" + roleId);
            throw exception;
        }
        return new DiscordChannel(groupId, channelId, roleId);
    }

    /** Deletes the channel and its role. */
    @Override
    public void deleteChannel(DiscordChannel channel) {
        send(botRequest("/channels/" + channel.getChannelId(), "DELETE", null));
        send(botRequest("/guilds/" + guildId + "/roles/" + channel.getRoleId(), "DELETE", null));
    }

    /** Gives a server member the role, so they can see its channel. */
    @Override
    public void grantRole(String discordUserId, String roleId) {
        send(botRequest("/guilds/" + guildId + "/members/" + discordUserId + "/roles/" + roleId, "PUT", null));
    }

    /** Takes the role away from a server member, so its channel disappears for them. */
    @Override
    public void revokeRole(String discordUserId, String roleId) {
        send(botRequest("/guilds/" + guildId + "/members/" + discordUserId + "/roles/" + roleId, "DELETE", null));
    }

    /** Returns the web address that opens the channel in Discord. */
    @Override
    public String channelUrl(String channelId) {
        return "https://discord.com/channels/" + guildId + "/" + channelId;
    }

    /** Returns the bot's own Discord user id, asking Discord once and then remembering it. */
    private String botUserId() {
        if (botUserId == null) {
            botUserId = send(botRequest("/users/@me", "GET", null)).path("id").asText();
        }
        return botUserId;
    }

    /** Builds a request signed as the bot, with a JSON body or none. */
    private HttpRequest botRequest(String path, String method, JsonNode body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + path))
                .timeout(TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bot " + botToken);
        if (body == null) {
            return builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
        }
        return builder.header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
    }

    /** Sends a request and returns the JSON reply (empty when there is none); throws DiscordException on failure. */
    private JsonNode send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new DiscordException("Could not reach Discord", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DiscordException("Interrupted while calling Discord", exception);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new DiscordException("Discord refused " + request.method() + " " + request.uri().getPath()
                    + " with status " + response.statusCode() + ": " + response.body());
        }
        if (response.body() == null || response.body().isBlank()) {
            return json.createObjectNode();
        }
        try {
            return json.readTree(response.body());
        } catch (IOException exception) {
            throw new DiscordException("Discord sent a reply that is not JSON", exception);
        }
    }

    /** Sends a DELETE and ignores any failure; used to tidy up after a half-finished change. */
    private void deleteQuietly(String path) {
        try {
            send(botRequest(path, "DELETE", null));
        } catch (DiscordException ignored) {
            // Nothing more to do: the original failure is what gets reported.
        }
    }

    /** Builds one permission overwrite entry; allow or deny may be null. */
    private ObjectNode overwrite(String id, int type, String allow, String deny) {
        ObjectNode entry = json.createObjectNode();
        entry.put("id", id);
        entry.put("type", type);
        entry.put("allow", allow == null ? "0" : allow);
        entry.put("deny", deny == null ? "0" : deny);
        return entry;
    }

    /** Turns a group name into a Discord channel name: lowercase letters, digits and single hyphens. */
    private String toChannelName(String name) {
        StringBuilder result = new StringBuilder();
        String lower = name == null ? "" : name.toLowerCase();
        for (int i = 0; i < lower.length() && result.length() < MAX_CHANNEL_NAME; i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                result.append(c);
            } else if (result.length() > 0 && result.charAt(result.length() - 1) != '-') {
                result.append('-');
            }
        }
        while (result.length() > 0 && result.charAt(result.length() - 1) == '-') {
            result.deleteCharAt(result.length() - 1);
        }
        if (result.length() == 0) {
            return "study-group";
        }
        return result.toString();
    }

    /** Returns a setting's value, trimmed, or an empty string if it is missing. */
    private static String setting(Environment environment, String key) {
        String value = environment.getProperty(key);
        return value == null ? "" : value.trim();
    }

    /** URL-encodes a value for a query string or form body. */
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
