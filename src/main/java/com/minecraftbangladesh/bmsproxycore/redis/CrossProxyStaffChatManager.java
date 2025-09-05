package com.minecraftbangladesh.bmsproxycore.redis;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

/**
 * Manages cross-proxy staff chat messaging via Redis pub/sub
 * Handles message serialization, deserialization, and broadcasting
 */
public class CrossProxyStaffChatManager {
    
    private final BMSProxyCore plugin;
    private final RedisManager redisManager;
    private StaffChatPubSubListener pubSubListener;
    
    // Message types for Redis communication
    private static final String MESSAGE_TYPE_CHAT = "chat";
    private static final String MESSAGE_TYPE_DISCORD = "discord";
    private static final String MESSAGE_TYPE_ACTIVITY_CONNECT = "activity_connect";
    private static final String MESSAGE_TYPE_ACTIVITY_DISCONNECT = "activity_disconnect";
    private static final String MESSAGE_TYPE_ACTIVITY_SERVER_SWITCH = "activity_server_switch";
    
    public CrossProxyStaffChatManager(BMSProxyCore plugin, RedisManager redisManager) {
        this.plugin = plugin;
        this.redisManager = redisManager;
    }
    
    /**
     * Initialize cross-proxy staff chat functionality
     */
    public void initialize() {
        if (!plugin.getConfigManager().isRedisEnabled()) {
            plugin.getLogger().info("Redis cross-proxy staff chat is disabled");
            return;
        }
        
        // Create and start pub/sub listener
        pubSubListener = new StaffChatPubSubListener();
        String chatChannel = plugin.getConfigManager().getRedisChatChannel();
        String activityChannel = plugin.getConfigManager().getRedisActivityChannel();
        
        redisManager.subscribe(pubSubListener, chatChannel, activityChannel);
        
        plugin.getLogger().info("Cross-proxy staff chat initialized");
        plugin.getLogger().info("Listening on channels: " + chatChannel + ", " + activityChannel);
    }
    
    /**
     * Broadcast a staff chat message to other proxies
     */
    public void broadcastStaffChatMessage(Player player, String message) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            String serverName = player.getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("Unknown");
            
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_CHAT);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("player_name", player.getUsername());
            messageData.put("player_uuid", player.getUniqueId().toString());
            messageData.put("server_name", serverName);
            messageData.put("message", message);
            messageData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getRedisChatChannel();
            redisManager.publishMessage(channel, messageData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast staff chat message", e);
        }
    }
    
    /**
     * Broadcast a staff chat message from console to other proxies
     */
    public void broadcastConsoleStaffChatMessage(String message) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_CHAT);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("player_name", "Console");
            messageData.put("player_uuid", "00000000-0000-0000-0000-000000000000");
            messageData.put("server_name", "Console");
            messageData.put("message", message);
            messageData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getRedisChatChannel();
            redisManager.publishMessage(channel, messageData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast console staff chat message", e);
        }
    }
    
    /**
     * Broadcast player connect activity to other proxies
     */
    public void broadcastPlayerConnect(Player player) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_ACTIVITY_CONNECT);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("player_name", player.getUsername());
            messageData.put("player_uuid", player.getUniqueId().toString());
            messageData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getRedisActivityChannel();
            redisManager.publishMessage(channel, messageData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast player connect activity", e);
        }
    }
    
    /**
     * Broadcast player disconnect activity to other proxies
     */
    public void broadcastPlayerDisconnect(Player player) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_ACTIVITY_DISCONNECT);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("player_name", player.getUsername());
            messageData.put("player_uuid", player.getUniqueId().toString());
            messageData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getRedisActivityChannel();
            redisManager.publishMessage(channel, messageData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast player disconnect activity", e);
        }
    }
    
    /**
     * Broadcast player server switch activity to other proxies
     */
    public void broadcastPlayerServerSwitch(Player player, String fromServer, String toServer) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_ACTIVITY_SERVER_SWITCH);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("player_name", player.getUsername());
            messageData.put("player_uuid", player.getUniqueId().toString());
            messageData.put("from_server", fromServer);
            messageData.put("to_server", toServer);
            messageData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getRedisActivityChannel();
            redisManager.publishMessage(channel, messageData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast player server switch activity", e);
        }
    }
    
    /**
     * Shutdown cross-proxy staff chat manager
     */
    public void shutdown() {
        if (pubSubListener != null && pubSubListener.isSubscribed()) {
            pubSubListener.unsubscribe();
        }
        plugin.getLogger().info("Cross-proxy staff chat manager shutdown");
    }
    
    /**
     * Redis pub/sub listener for staff chat messages
     */
    private class StaffChatPubSubListener extends JedisPubSub {
        
        @Override
        public void onMessage(String channel, String message) {
            try {
                JSONObject messageData = new JSONObject(message);
                String sourceProxyId = messageData.getString("proxy_id");
                String currentProxyId = plugin.getConfigManager().getRedisProxyId();
                
                // Ignore messages from our own proxy to prevent loops
                if (sourceProxyId.equals(currentProxyId)) {
                    return;
                }
                
                String messageType = messageData.getString("type");
                
                switch (messageType) {
                    case MESSAGE_TYPE_CHAT:
                        handleCrossProxyChatMessage(messageData);
                        break;
                    case MESSAGE_TYPE_DISCORD:
                        handleCrossProxyDiscordMessage(messageData);
                        break;
                    case MESSAGE_TYPE_ACTIVITY_CONNECT:
                        handleCrossProxyConnectActivity(messageData);
                        break;
                    case MESSAGE_TYPE_ACTIVITY_DISCONNECT:
                        handleCrossProxyDisconnectActivity(messageData);
                        break;
                    case MESSAGE_TYPE_ACTIVITY_SERVER_SWITCH:
                        handleCrossProxyServerSwitchActivity(messageData);
                        break;
                    default:
                        plugin.getLogger().warn("Unknown cross-proxy message type: " + messageType);
                }
                
            } catch (Exception e) {
                plugin.getLogger().error("Failed to process cross-proxy message", e);
            }
        }
        
        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            plugin.getLogger().info("Subscribed to Redis channel: " + channel);
        }
        
        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            plugin.getLogger().info("Unsubscribed from Redis channel: " + channel);
        }
    }

    /**
     * Handle incoming cross-proxy chat messages
     */
    private void handleCrossProxyChatMessage(JSONObject messageData) {
        try {
            String playerName = messageData.getString("player_name");
            String serverName = messageData.getString("server_name");
            String messageText = messageData.getString("message");
            String sourceProxyId = messageData.getString("proxy_id");

            Component formattedMessage;

            if ("Console".equals(playerName)) {
                // Format console message
                formattedMessage = MessageUtils.formatMessage(
                    plugin.getConfigManager().getCrossProxyMessageFormat()
                        .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                        .replace("{proxy}", sourceProxyId)
                        .replace("{server}", serverName)
                        .replace("{player}", playerName)
                        .replace("{message}", messageText)
                );
            } else {
                // Format player message
                formattedMessage = MessageUtils.formatMessage(
                    plugin.getConfigManager().getCrossProxyMessageFormat()
                        .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                        .replace("{proxy}", sourceProxyId)
                        .replace("{server}", serverName)
                        .replace("{player}", playerName)
                        .replace("{message}", messageText)
                );
            }

            // Broadcast to local staff members
            String usePermission = plugin.getConfigManager().getStaffChatUsePermission();
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, usePermission);

            // Log to console
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy chat message", e);
        }
    }

    /**
     * Broadcast a Discord-originated staff chat message to other proxies
     */
    public void broadcastDiscordStaffChatMessage(String discordUsername, String message) {
        if (!redisManager.isConnected()) {
            return;
        }

        try {
            JSONObject messageData = new JSONObject();
            messageData.put("type", MESSAGE_TYPE_DISCORD);
            messageData.put("proxy_id", plugin.getConfigManager().getRedisProxyId());
            messageData.put("discord_username", discordUsername);
            messageData.put("message", message);
            messageData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getRedisChatChannel();
            redisManager.publishMessage(channel, messageData.toString());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast discord staff chat message", e);
        }
    }

    /**
     * Handle incoming Discord-originated staff chat messages from other proxies
     */
    private void handleCrossProxyDiscordMessage(JSONObject messageData) {
        try {
            String discordUsername = messageData.getString("discord_username");
            String messageText = messageData.getString("message");
            String sourceProxyId = messageData.getString("proxy_id");

            Component formattedMessage = MessageUtils.formatMessage(
                plugin.getConfigManager().getCrossProxyMessageFormat()
                    .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                    .replace("{proxy}", sourceProxyId)
                    .replace("{server}", "Discord")
                    .replace("{player}", discordUsername)
                    .replace("{message}", messageText)
            );

            String usePermission = plugin.getConfigManager().getStaffChatUsePermission();
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, usePermission);
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy discord message", e);
        }
    }

    /**
     * Handle incoming cross-proxy connect activity
     */
    private void handleCrossProxyConnectActivity(JSONObject messageData) {
        try {
            String playerName = messageData.getString("player_name");
            String sourceProxyId = messageData.getString("proxy_id");

            Component formattedMessage = MessageUtils.formatMessage(
                plugin.getConfigManager().getCrossProxyConnectFormat()
                    .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                    .replace("{player}", playerName)
                    .replace("{proxy}", sourceProxyId)
            );

            // Broadcast to local staff members with activity permission
            String activityPermission = plugin.getConfigManager().getStaffChatActivityPermission();
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, activityPermission);

            // Log to console
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy connect activity", e);
        }
    }

    /**
     * Handle incoming cross-proxy disconnect activity
     */
    private void handleCrossProxyDisconnectActivity(JSONObject messageData) {
        try {
            String playerName = messageData.getString("player_name");
            String sourceProxyId = messageData.getString("proxy_id");

            Component formattedMessage = MessageUtils.formatMessage(
                plugin.getConfigManager().getCrossProxyDisconnectFormat()
                    .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                    .replace("{player}", playerName)
                    .replace("{proxy}", sourceProxyId)
            );

            // Broadcast to local staff members with activity permission
            String activityPermission = plugin.getConfigManager().getStaffChatActivityPermission();
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, activityPermission);

            // Log to console
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy disconnect activity", e);
        }
    }

    /**
     * Handle incoming cross-proxy server switch activity
     */
    private void handleCrossProxyServerSwitchActivity(JSONObject messageData) {
        try {
            String playerName = messageData.getString("player_name");
            String fromServer = messageData.getString("from_server");
            String toServer = messageData.getString("to_server");
            String sourceProxyId = messageData.getString("proxy_id");

            Component formattedMessage = MessageUtils.formatMessage(
                plugin.getConfigManager().getCrossProxyServerSwitchFormat()
                    .replace("{prefix}", plugin.getConfigManager().getStaffChatPrefix())
                    .replace("{player}", playerName)
                    .replace("{from_server}", fromServer)
                    .replace("{to_server}", toServer)
                    .replace("{proxy}", sourceProxyId)
            );

            // Broadcast to local staff members with activity permission
            String activityPermission = plugin.getConfigManager().getStaffChatActivityPermission();
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, activityPermission);

            // Log to console
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy server switch activity", e);
        }
    }
}
