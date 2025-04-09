package com.miecraftbangladesh.bmsproxycore.utils;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.velocitypowered.api.proxy.Player;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class DiscordWebhook {

    private final BMSProxyCore plugin;
    private final OkHttpClient httpClient;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public DiscordWebhook(BMSProxyCore plugin) {
        this.plugin = plugin;
        this.httpClient = new OkHttpClient();
    }

    /**
     * Send a staff chat message to Discord using a webhook
     *
     * @param player  The player who sent the message
     * @param message The message content
     * @param server  The server name the player is on
     */
    public void sendStaffChatMessage(Player player, String message, String server) {
        if (!plugin.getConfigManager().isDiscordEnabled()) {
            return;
        }

        String webhookUrl = plugin.getConfigManager().getDiscordWebhookUrl();
        if (webhookUrl.isEmpty() || webhookUrl.equals("https://discord.com/api/webhooks/your-webhook-url-here")) {
            plugin.getLogger().warn("Discord webhook URL is not configured properly.");
            return;
        }

        // Format the message according to config
        String formattedMessage = plugin.getConfigManager().getDiscordMessageFormat()
                .replace("{player}", player.getUsername())
                .replace("{server}", server)
                .replace("{message}", message);

        // Create webhook payload
        JSONObject json = new JSONObject();
        json.put("content", formattedMessage);
        
        // Set webhook name if configured
        String webhookName = plugin.getConfigManager().getDiscordWebhookName();
        if (!webhookName.isEmpty()) {
            json.put("username", webhookName);
        }

        // Set avatar URL using Crafatar if enabled
        if (plugin.getConfigManager().usePlayerAvatar()) {
            String avatarUrl = getCrafatarAvatarUrl(player.getUniqueId().toString());
            if (!avatarUrl.isEmpty()) {
                json.put("avatar_url", avatarUrl);
            }
        }

        // Send the webhook request
        sendWebhookRequest(webhookUrl, json);
    }
    
    /**
     * Send a staff chat message from console to Discord using a webhook
     *
     * @param message The message content
     */
    public void sendConsoleStaffChatMessage(String message) {
        if (!plugin.getConfigManager().isDiscordEnabled()) {
            return;
        }

        String webhookUrl = plugin.getConfigManager().getDiscordWebhookUrl();
        if (webhookUrl.isEmpty() || webhookUrl.equals("https://discord.com/api/webhooks/your-webhook-url-here")) {
            plugin.getLogger().warn("Discord webhook URL is not configured properly.");
            return;
        }

        // Format the message according to config
        String formattedMessage = plugin.getConfigManager().getDiscordMessageFormat()
                .replace("{player}", "Console")
                .replace("{server}", "Console")
                .replace("{message}", message);

        // Create webhook payload
        JSONObject json = new JSONObject();
        json.put("content", formattedMessage);
        
        // Set webhook name if configured
        String webhookName = plugin.getConfigManager().getDiscordWebhookName();
        if (!webhookName.isEmpty()) {
            json.put("username", webhookName);
        }
        
        // You can set a console avatar URL here if desired
        // json.put("avatar_url", "https://your-console-avatar-url.png");

        // Send the webhook request
        sendWebhookRequest(webhookUrl, json);
    }
    
    /**
     * Send a webhook request to Discord
     *
     * @param webhookUrl The Discord webhook URL
     * @param json The JSON payload to send
     */
    private void sendWebhookRequest(String webhookUrl, JSONObject json) {
        // Build request
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        // Send asynchronously
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                plugin.getLogger().error("Failed to send Discord webhook", e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                response.close();
                if (!response.isSuccessful()) {
                    plugin.getLogger().error("Discord webhook responded with non-OK status: " + response.code());
                }
            }
        });
    }

    /**
     * Generate a Crafatar avatar URL for the player's UUID
     *
     * @param uuid The player's UUID
     * @return The URL to the avatar image
     */
    private String getCrafatarAvatarUrl(String uuid) {
        StringBuilder urlBuilder = new StringBuilder("https://crafatar.com/avatars/");
        urlBuilder.append(uuid);
        
        boolean hasParams = false;
        
        // Add size parameter if configured
        int size = plugin.getConfigManager().getAvatarSize();
        if (size > 0 && size <= 512) {
            urlBuilder.append("?size=").append(size);
            hasParams = true;
        }
        
        // Add overlay parameter if configured
        if (plugin.getConfigManager().useAvatarOverlay()) {
            urlBuilder.append(hasParams ? "&" : "?").append("overlay");
        }
        
        return urlBuilder.toString();
    }
} 