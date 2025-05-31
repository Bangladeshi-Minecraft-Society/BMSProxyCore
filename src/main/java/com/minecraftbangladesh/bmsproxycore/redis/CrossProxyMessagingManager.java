package com.minecraftbangladesh.bmsproxycore.redis;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Manages cross-proxy private messaging via Redis pub/sub
 * Handles player lookups, message delivery, reply targets, and social spy across proxies
 */
public class CrossProxyMessagingManager {
    
    private final BMSProxyCore plugin;
    private final RedisManager redisManager;
    private PrivateMessagePubSubListener pubSubListener;
    
    // Cache for cross-proxy player lookups
    private final Map<String, CompletableFuture<PlayerLookupResult>> pendingLookups = new ConcurrentHashMap<>();

    // Cache for cross-proxy players (for tab completion)
    private final Map<String, CrossProxyPlayer> crossProxyPlayers = new ConcurrentHashMap<>();

    // Message types for Redis communication
    private static final String MESSAGE_TYPE_PRIVATE_MESSAGE = "private_message";
    private static final String MESSAGE_TYPE_PLAYER_LOOKUP_REQUEST = "player_lookup_request";
    private static final String MESSAGE_TYPE_PLAYER_LOOKUP_RESPONSE = "player_lookup_response";
    private static final String MESSAGE_TYPE_SOCIAL_SPY = "social_spy";
    private static final String MESSAGE_TYPE_REPLY_TARGET_UPDATE = "reply_target_update";
    private static final String MESSAGE_TYPE_PLAYER_JOIN = "player_join";
    private static final String MESSAGE_TYPE_PLAYER_LEAVE = "player_leave";
    private static final String MESSAGE_TYPE_PLAYER_LIST_REQUEST = "player_list_request";
    private static final String MESSAGE_TYPE_PLAYER_LIST_RESPONSE = "player_list_response";
    
    // Lookup timeout in seconds
    private static final int LOOKUP_TIMEOUT_SECONDS = 5;
    
    public CrossProxyMessagingManager(BMSProxyCore plugin, RedisManager redisManager) {
        this.plugin = plugin;
        this.redisManager = redisManager;
    }
    
    /**
     * Initialize cross-proxy private messaging functionality
     */
    public void initialize() {
        if (!plugin.getConfigManager().isPrivateMessagesRedisEnabled()) {
            plugin.getLogger().info("Redis cross-proxy private messaging is disabled");
            return;
        }
        
        // Create and start pub/sub listener
        pubSubListener = new PrivateMessagePubSubListener();
        String messageChannel = plugin.getConfigManager().getPrivateMessagesMessageChannel();
        String lookupChannel = plugin.getConfigManager().getPrivateMessagesLookupChannel();
        String lookupResponseChannel = plugin.getConfigManager().getPrivateMessagesLookupResponseChannel();
        String socialSpyChannel = plugin.getConfigManager().getPrivateMessagesSocialSpyChannel();
        String replyChannel = plugin.getConfigManager().getPrivateMessagesReplyChannel();
        
        redisManager.subscribe(pubSubListener, messageChannel, lookupChannel, lookupResponseChannel, socialSpyChannel, replyChannel);
        
        plugin.getLogger().info("Cross-proxy private messaging initialized");
        plugin.getLogger().info("Listening on channels: " + messageChannel + ", " + lookupChannel + ", " + lookupResponseChannel + ", " + socialSpyChannel + ", " + replyChannel);

        // Request initial player list from all proxies
        requestPlayerListFromAllProxies();
    }
    
    /**
     * Attempt to send a cross-proxy private message
     * @param senderName The name of the sender
     * @param senderUUID The UUID of the sender
     * @param targetName The name of the target player
     * @param message The message content
     * @return CompletableFuture that completes with true if message was sent, false if player not found
     */
    public CompletableFuture<Boolean> sendCrossProxyMessage(String senderName, UUID senderUUID, String targetName, String message) {
        if (!redisManager.isConnected()) {
            return CompletableFuture.completedFuture(false);
        }
        
        // First, lookup the target player across all proxies
        return lookupPlayer(targetName).thenCompose(lookupResult -> {
            if (lookupResult == null || !lookupResult.found) {
                return CompletableFuture.completedFuture(false);
            }
            
            // Send the message to the target proxy
            try {
                JSONObject messageData = new JSONObject();
                messageData.put("type", MESSAGE_TYPE_PRIVATE_MESSAGE);
                messageData.put("sender_proxy", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
                messageData.put("target_proxy", lookupResult.proxyId);
                messageData.put("sender_name", senderName);
                messageData.put("sender_uuid", senderUUID.toString());
                messageData.put("target_name", targetName);
                messageData.put("target_uuid", lookupResult.playerUUID.toString());
                messageData.put("message", message);
                messageData.put("timestamp", System.currentTimeMillis());
                
                String channel = plugin.getConfigManager().getPrivateMessagesMessageChannel();
                return redisManager.publishMessage(channel, messageData.toString());
                
            } catch (Exception e) {
                plugin.getLogger().error("Failed to send cross-proxy private message", e);
                return CompletableFuture.completedFuture(false);
            }
        });
    }
    
    /**
     * Lookup a player across all connected proxies
     * @param playerName The name of the player to lookup
     * @return CompletableFuture that completes with the lookup result
     */
    public CompletableFuture<PlayerLookupResult> lookupPlayer(String playerName) {
        if (!redisManager.isConnected()) {
            return CompletableFuture.completedFuture(new PlayerLookupResult(false, null, null));
        }
        
        String lookupId = UUID.randomUUID().toString();
        CompletableFuture<PlayerLookupResult> future = new CompletableFuture<>();
        
        // Store the pending lookup
        pendingLookups.put(lookupId, future);
        
        // Set timeout
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            CompletableFuture<PlayerLookupResult> pendingFuture = pendingLookups.remove(lookupId);
            if (pendingFuture != null && !pendingFuture.isDone()) {
                pendingFuture.complete(new PlayerLookupResult(false, null, null));
            }
        }).delay(LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS).schedule();
        
        // Send lookup request
        try {
            JSONObject lookupData = new JSONObject();
            lookupData.put("type", MESSAGE_TYPE_PLAYER_LOOKUP_REQUEST);
            lookupData.put("lookup_id", lookupId);
            lookupData.put("requesting_proxy", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            lookupData.put("player_name", playerName);
            lookupData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getPrivateMessagesLookupChannel();
            redisManager.publishMessage(channel, lookupData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to send player lookup request", e);
            pendingLookups.remove(lookupId);
            future.complete(new PlayerLookupResult(false, null, null));
        }
        
        return future;
    }
    
    /**
     * Broadcast social spy message to other proxies
     */
    public void broadcastSocialSpyMessage(String senderName, UUID senderUUID, String receiverName, UUID receiverUUID, String message, String receiverProxy) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject spyData = new JSONObject();
            spyData.put("type", MESSAGE_TYPE_SOCIAL_SPY);
            spyData.put("sender_proxy", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            spyData.put("receiver_proxy", receiverProxy);
            spyData.put("sender_name", senderName);
            spyData.put("sender_uuid", senderUUID.toString());
            spyData.put("receiver_name", receiverName);
            spyData.put("receiver_uuid", receiverUUID.toString());
            spyData.put("message", message);
            spyData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getPrivateMessagesSocialSpyChannel();
            redisManager.publishMessage(channel, spyData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast social spy message", e);
        }
    }
    
    /**
     * Update reply targets across proxies
     */
    public void updateCrossProxyReplyTarget(UUID playerUUID, String playerName, UUID targetUUID, String targetName, String targetProxy) {
        if (!redisManager.isConnected()) {
            return;
        }
        
        try {
            JSONObject replyData = new JSONObject();
            replyData.put("type", MESSAGE_TYPE_REPLY_TARGET_UPDATE);
            replyData.put("source_proxy", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            replyData.put("target_proxy", targetProxy);
            replyData.put("player_uuid", playerUUID.toString());
            replyData.put("player_name", playerName);
            replyData.put("target_uuid", targetUUID.toString());
            replyData.put("target_name", targetName);
            replyData.put("timestamp", System.currentTimeMillis());
            
            String channel = plugin.getConfigManager().getPrivateMessagesReplyChannel();
            redisManager.publishMessage(channel, replyData.toString());
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to update cross-proxy reply target", e);
        }
    }
    
    /**
     * Broadcast player join event to other proxies
     */
    public void broadcastPlayerJoin(Player player) {
        if (!redisManager.isConnected()) {
            return;
        }

        try {
            JSONObject joinData = new JSONObject();
            joinData.put("type", MESSAGE_TYPE_PLAYER_JOIN);
            joinData.put("proxy_id", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            joinData.put("player_name", player.getUsername());
            joinData.put("player_uuid", player.getUniqueId().toString());
            joinData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getPrivateMessagesMessageChannel();
            redisManager.publishMessage(channel, joinData.toString());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast player join", e);
        }
    }

    /**
     * Broadcast player leave event to other proxies
     */
    public void broadcastPlayerLeave(Player player) {
        if (!redisManager.isConnected()) {
            return;
        }

        try {
            JSONObject leaveData = new JSONObject();
            leaveData.put("type", MESSAGE_TYPE_PLAYER_LEAVE);
            leaveData.put("proxy_id", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            leaveData.put("player_name", player.getUsername());
            leaveData.put("player_uuid", player.getUniqueId().toString());
            leaveData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getPrivateMessagesMessageChannel();
            redisManager.publishMessage(channel, leaveData.toString());

            // Remove from local cache
            crossProxyPlayers.remove(player.getUsername().toLowerCase());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to broadcast player leave", e);
        }
    }

    /**
     * Request player list from all proxies for tab completion
     */
    public void requestPlayerListFromAllProxies() {
        if (!redisManager.isConnected()) {
            return;
        }

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("type", MESSAGE_TYPE_PLAYER_LIST_REQUEST);
            requestData.put("requesting_proxy", plugin.getConfigManager().getPrivateMessagesRedisProxyId());
            requestData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getPrivateMessagesLookupChannel();
            redisManager.publishMessage(channel, requestData.toString());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to request player list", e);
        }
    }

    /**
     * Get all known players for tab completion (local + cross-proxy)
     */
    public List<String> getAllKnownPlayerNames() {
        List<String> allPlayers = new ArrayList<>();

        // Add local players
        for (Player player : plugin.getServer().getAllPlayers()) {
            allPlayers.add(player.getUsername());
        }

        // Add cross-proxy players (remove expired ones)
        long maxAge = 300000; // 5 minutes
        crossProxyPlayers.entrySet().removeIf(entry -> entry.getValue().isExpired(maxAge));

        for (CrossProxyPlayer crossProxyPlayer : crossProxyPlayers.values()) {
            if (!allPlayers.contains(crossProxyPlayer.name)) {
                allPlayers.add(crossProxyPlayer.name);
            }
        }

        return allPlayers;
    }

    /**
     * Get filtered player names for tab completion
     */
    public List<String> getFilteredPlayerNames(String partialName) {
        String lowerPartial = partialName.toLowerCase();
        return getAllKnownPlayerNames().stream()
                .filter(name -> name.toLowerCase().startsWith(lowerPartial))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Shutdown cross-proxy messaging manager
     */
    public void shutdown() {
        if (pubSubListener != null && pubSubListener.isSubscribed()) {
            pubSubListener.unsubscribe();
        }
        
        // Complete any pending lookups
        for (CompletableFuture<PlayerLookupResult> future : pendingLookups.values()) {
            if (!future.isDone()) {
                future.complete(new PlayerLookupResult(false, null, null));
            }
        }
        pendingLookups.clear();
        
        plugin.getLogger().info("Cross-proxy messaging manager shutdown");
    }
    
    /**
     * Result of a cross-proxy player lookup
     */
    public static class PlayerLookupResult {
        public final boolean found;
        public final UUID playerUUID;
        public final String proxyId;

        public PlayerLookupResult(boolean found, UUID playerUUID, String proxyId) {
            this.found = found;
            this.playerUUID = playerUUID;
            this.proxyId = proxyId;
        }
    }

    /**
     * Represents a player on another proxy for caching purposes
     */
    public static class CrossProxyPlayer {
        public final String name;
        public final UUID uuid;
        public final String proxyId;
        public final long lastSeen;

        public CrossProxyPlayer(String name, UUID uuid, String proxyId) {
            this.name = name;
            this.uuid = uuid;
            this.proxyId = proxyId;
            this.lastSeen = System.currentTimeMillis();
        }

        public boolean isExpired(long maxAgeMs) {
            return System.currentTimeMillis() - lastSeen > maxAgeMs;
        }
    }
    
    /**
     * Redis pub/sub listener for private messages
     */
    private class PrivateMessagePubSubListener extends JedisPubSub {
        
        @Override
        public void onMessage(String channel, String message) {
            try {
                JSONObject messageData = new JSONObject(message);
                String messageType = messageData.getString("type");
                
                switch (messageType) {
                    case MESSAGE_TYPE_PRIVATE_MESSAGE:
                        handleCrossProxyPrivateMessage(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_LOOKUP_REQUEST:
                        handlePlayerLookupRequest(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_LOOKUP_RESPONSE:
                        handlePlayerLookupResponse(messageData);
                        break;
                    case MESSAGE_TYPE_SOCIAL_SPY:
                        handleCrossProxySocialSpy(messageData);
                        break;
                    case MESSAGE_TYPE_REPLY_TARGET_UPDATE:
                        handleReplyTargetUpdate(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_JOIN:
                        handlePlayerJoin(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_LEAVE:
                        handlePlayerLeave(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_LIST_REQUEST:
                        handlePlayerListRequest(messageData);
                        break;
                    case MESSAGE_TYPE_PLAYER_LIST_RESPONSE:
                        handlePlayerListResponse(messageData);
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
     * Handle incoming cross-proxy private messages
     */
    private void handleCrossProxyPrivateMessage(JSONObject messageData) {
        try {
            String targetProxy = messageData.getString("target_proxy");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Only process messages intended for this proxy
            if (!targetProxy.equals(currentProxy)) {
                return;
            }

            String senderName = messageData.getString("sender_name");
            UUID senderUUID = UUID.fromString(messageData.getString("sender_uuid"));
            String targetName = messageData.getString("target_name");
            UUID targetUUID = UUID.fromString(messageData.getString("target_uuid"));
            String message = messageData.getString("message");
            String senderProxy = messageData.getString("sender_proxy");

            // Find the target player on this proxy
            Player targetPlayer = plugin.getServer().getPlayer(targetUUID).orElse(null);
            if (targetPlayer == null) {
                // Player not found, send delivery failure notification back
                sendDeliveryFailureNotification(senderProxy, senderName, targetName);
                return;
            }

            // Check if target player has message toggle disabled
            if (plugin.getMessagingManager().isMessageToggleDisabled(targetUUID)) {
                // Check if sender has bypass permission (this is tricky cross-proxy, so we'll allow it for now)
                // In a full implementation, you might want to cache permission info or send it with the message
            }

            // Check if target player is ignoring the sender
            if (plugin.getMessagingManager().isPlayerIgnoring(targetUUID, senderUUID)) {
                // Player is ignoring sender, don't deliver message
                return;
            }

            // Update reply targets
            plugin.getMessagingManager().setReplyTarget(targetUUID, senderUUID);

            // Format and send message to target player
            String receiverFormat = plugin.getConfigManager().getCrossProxyPrivateMessageReceiverFormat()
                    .replace("{sender}", senderName)
                    .replace("{proxy}", senderProxy)
                    .replace("{message}", message);
            targetPlayer.sendMessage(MessageUtils.formatMessage(receiverFormat));

            // Send social spy notifications to local players
            broadcastLocalSocialSpy(senderName, senderUUID, targetName, targetUUID, message, senderProxy);

            // Send confirmation back to sender proxy
            sendDeliveryConfirmation(senderProxy, senderName, targetName);

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy private message", e);
        }
    }

    /**
     * Handle player lookup requests
     */
    private void handlePlayerLookupRequest(JSONObject messageData) {
        try {
            String lookupId = messageData.getString("lookup_id");
            String requestingProxy = messageData.getString("requesting_proxy");
            String playerName = messageData.getString("player_name");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Don't respond to our own requests
            if (requestingProxy.equals(currentProxy)) {
                return;
            }

            // Check if player is on this proxy
            Player player = plugin.getServer().getPlayer(playerName).orElse(null);

            // Send response
            JSONObject responseData = new JSONObject();
            responseData.put("type", MESSAGE_TYPE_PLAYER_LOOKUP_RESPONSE);
            responseData.put("lookup_id", lookupId);
            responseData.put("requesting_proxy", requestingProxy);
            responseData.put("responding_proxy", currentProxy);
            responseData.put("player_name", playerName);
            responseData.put("found", player != null);
            if (player != null) {
                responseData.put("player_uuid", player.getUniqueId().toString());
            }
            responseData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getPrivateMessagesLookupResponseChannel();
            redisManager.publishMessage(channel, responseData.toString());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player lookup request", e);
        }
    }

    /**
     * Handle player lookup responses
     */
    private void handlePlayerLookupResponse(JSONObject messageData) {
        try {
            String lookupId = messageData.getString("lookup_id");
            String requestingProxy = messageData.getString("requesting_proxy");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Only process responses intended for this proxy
            if (!requestingProxy.equals(currentProxy)) {
                return;
            }

            CompletableFuture<PlayerLookupResult> future = pendingLookups.remove(lookupId);
            if (future != null && !future.isDone()) {
                boolean found = messageData.getBoolean("found");
                if (found) {
                    String respondingProxy = messageData.getString("responding_proxy");
                    UUID playerUUID = UUID.fromString(messageData.getString("player_uuid"));
                    future.complete(new PlayerLookupResult(true, playerUUID, respondingProxy));
                } else {
                    // Continue waiting for other responses, this will timeout eventually
                }
            }

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player lookup response", e);
        }
    }

    /**
     * Handle cross-proxy social spy messages
     */
    private void handleCrossProxySocialSpy(JSONObject messageData) {
        try {
            String senderName = messageData.getString("sender_name");
            String receiverName = messageData.getString("receiver_name");
            String message = messageData.getString("message");
            String senderProxy = messageData.getString("sender_proxy");
            String receiverProxy = messageData.getString("receiver_proxy");

            // Format social spy message
            String socialSpyFormat = plugin.getConfigManager().getCrossProxyPrivateMessageSocialSpyFormat()
                    .replace("{sender}", senderName)
                    .replace("{sender_proxy}", senderProxy)
                    .replace("{receiver}", receiverName)
                    .replace("{receiver_proxy}", receiverProxy)
                    .replace("{message}", message);

            Component formattedMessage = MessageUtils.formatMessage(socialSpyFormat);

            // Send to local social spy users
            for (Player player : plugin.getServer().getAllPlayers()) {
                if (plugin.getMessagingManager().isSocialSpyEnabled(player.getUniqueId())) {
                    player.sendMessage(formattedMessage);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle cross-proxy social spy message", e);
        }
    }

    /**
     * Handle reply target updates
     */
    private void handleReplyTargetUpdate(JSONObject messageData) {
        try {
            String targetProxy = messageData.getString("target_proxy");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Only process updates intended for this proxy
            if (!targetProxy.equals(currentProxy)) {
                return;
            }

            UUID playerUUID = UUID.fromString(messageData.getString("player_uuid"));
            UUID targetUUID = UUID.fromString(messageData.getString("target_uuid"));

            // Update reply target for the player
            plugin.getMessagingManager().setReplyTarget(playerUUID, targetUUID);

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle reply target update", e);
        }
    }

    /**
     * Broadcast social spy message to local players only
     */
    private void broadcastLocalSocialSpy(String senderName, UUID senderUUID, String receiverName, UUID receiverUUID, String message, String senderProxy) {
        String socialSpyFormat = plugin.getConfigManager().getCrossProxyPrivateMessageSocialSpyFormat()
                .replace("{sender}", senderName)
                .replace("{sender_proxy}", senderProxy)
                .replace("{receiver}", receiverName)
                .replace("{receiver_proxy}", plugin.getConfigManager().getPrivateMessagesRedisProxyId())
                .replace("{message}", message);

        Component formattedMessage = MessageUtils.formatMessage(socialSpyFormat);

        for (Player player : plugin.getServer().getAllPlayers()) {
            if (plugin.getMessagingManager().isSocialSpyEnabled(player.getUniqueId())) {
                // Don't show social spy to the sender or receiver
                if (!player.getUniqueId().equals(senderUUID) && !player.getUniqueId().equals(receiverUUID)) {
                    player.sendMessage(formattedMessage);
                }
            }
        }
    }

    /**
     * Send delivery confirmation back to sender proxy
     */
    private void sendDeliveryConfirmation(String senderProxy, String senderName, String targetName) {
        // This could be implemented to send a confirmation message back to the sender
        // For now, we'll just log it
        plugin.getLogger().debug("Message delivered from " + senderName + " to " + targetName + " via proxy " + senderProxy);
    }

    /**
     * Send delivery failure notification back to sender proxy
     */
    private void sendDeliveryFailureNotification(String senderProxy, String senderName, String targetName) {
        // This could be implemented to send a failure notification back to the sender
        // For now, we'll just log it
        plugin.getLogger().warn("Failed to deliver message from " + senderName + " to " + targetName + " - player not found on proxy " + senderProxy);
    }

    /**
     * Handle player join notifications from other proxies
     */
    private void handlePlayerJoin(JSONObject messageData) {
        try {
            String proxyId = messageData.getString("proxy_id");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Don't process our own join events
            if (proxyId.equals(currentProxy)) {
                return;
            }

            String playerName = messageData.getString("player_name");
            UUID playerUUID = UUID.fromString(messageData.getString("player_uuid"));

            // Add to cross-proxy player cache
            crossProxyPlayers.put(playerName.toLowerCase(), new CrossProxyPlayer(playerName, playerUUID, proxyId));

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player join notification", e);
        }
    }

    /**
     * Handle player leave notifications from other proxies
     */
    private void handlePlayerLeave(JSONObject messageData) {
        try {
            String proxyId = messageData.getString("proxy_id");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Don't process our own leave events
            if (proxyId.equals(currentProxy)) {
                return;
            }

            String playerName = messageData.getString("player_name");

            // Remove from cross-proxy player cache
            crossProxyPlayers.remove(playerName.toLowerCase());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player leave notification", e);
        }
    }

    /**
     * Handle player list requests from other proxies
     */
    private void handlePlayerListRequest(JSONObject messageData) {
        try {
            String requestingProxy = messageData.getString("requesting_proxy");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Don't respond to our own requests
            if (requestingProxy.equals(currentProxy)) {
                return;
            }

            // Send our player list
            JSONObject responseData = new JSONObject();
            responseData.put("type", MESSAGE_TYPE_PLAYER_LIST_RESPONSE);
            responseData.put("requesting_proxy", requestingProxy);
            responseData.put("responding_proxy", currentProxy);

            // Add all local players
            List<String> playerNames = new ArrayList<>();
            List<String> playerUUIDs = new ArrayList<>();
            for (Player player : plugin.getServer().getAllPlayers()) {
                playerNames.add(player.getUsername());
                playerUUIDs.add(player.getUniqueId().toString());
            }

            responseData.put("player_names", playerNames);
            responseData.put("player_uuids", playerUUIDs);
            responseData.put("timestamp", System.currentTimeMillis());

            String channel = plugin.getConfigManager().getPrivateMessagesLookupResponseChannel();
            redisManager.publishMessage(channel, responseData.toString());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player list request", e);
        }
    }

    /**
     * Handle player list responses from other proxies
     */
    @SuppressWarnings("unchecked")
    private void handlePlayerListResponse(JSONObject messageData) {
        try {
            String requestingProxy = messageData.getString("requesting_proxy");
            String currentProxy = plugin.getConfigManager().getPrivateMessagesRedisProxyId();

            // Only process responses intended for this proxy
            if (!requestingProxy.equals(currentProxy)) {
                return;
            }

            String respondingProxy = messageData.getString("responding_proxy");
            List<String> playerNames = (List<String>) messageData.get("player_names");
            List<String> playerUUIDs = (List<String>) messageData.get("player_uuids");

            // Add players to cache
            for (int i = 0; i < playerNames.size() && i < playerUUIDs.size(); i++) {
                String playerName = playerNames.get(i);
                UUID playerUUID = UUID.fromString(playerUUIDs.get(i));
                crossProxyPlayers.put(playerName.toLowerCase(), new CrossProxyPlayer(playerName, playerUUID, respondingProxy));
            }

        } catch (Exception e) {
            plugin.getLogger().error("Failed to handle player list response", e);
        }
    }
}
