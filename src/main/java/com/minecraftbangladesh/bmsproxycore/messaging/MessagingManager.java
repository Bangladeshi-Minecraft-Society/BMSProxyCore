package com.minecraftbangladesh.bmsproxycore.messaging;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingManager {

    private final BMSProxyCore plugin;
    
    // Maps for tracking reply targets, social spy status, message toggle, and ignored players
    private final Map<UUID, UUID> replyTargets = new ConcurrentHashMap<>();
    private final Set<UUID> socialSpyEnabled = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UUID> messageToggleDisabled = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Set<UUID>> ignoredPlayers = new ConcurrentHashMap<>();

    public MessagingManager(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Send a private message from one player to another
     *
     * @param sender The player sending the message
     * @param receiver The player receiving the message
     * @param message The message content
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Player sender, Player receiver, String message) {
        UUID senderUUID = sender.getUniqueId();
        UUID receiverUUID = receiver.getUniqueId();
        
        // Check if sender is trying to message themselves
        if (senderUUID.equals(receiverUUID)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorMessageSelf()));
            return false;
        }
        
        // Check if receiver has message toggle disabled
        if (isMessageToggleDisabled(receiverUUID)) {
            String errorMessage = plugin.getConfigManager().getMessagingErrorPlayerToggled()
                    .replace("{player}", receiver.getUsername());
            sender.sendMessage(MessageUtils.formatMessage(errorMessage));
            return false;
        }
        
        // Check if receiver is ignoring the sender
        if (isPlayerIgnoring(receiverUUID, senderUUID)) {
            String errorMessage = plugin.getConfigManager().getMessagingErrorPlayerIgnored()
                    .replace("{player}", receiver.getUsername());
            sender.sendMessage(MessageUtils.formatMessage(errorMessage));
            return false;
        }

        // Check chat lock for private messages (if enabled)
        if (plugin.getConfigManager().isPrivateMessageChatLockRespected() &&
            plugin.getChatControlManager().isChatLocked()) {

            // Check bypass permission
            String bypassPermission = plugin.getConfigManager().getLockChatBypassPermission();
            if (bypassPermission.isEmpty() || !sender.hasPermission(bypassPermission)) {
                String errorMessage = plugin.getConfigManager().getPrivateMessageChatLockBlockedMessage();
                sender.sendMessage(MessageUtils.formatMessage(errorMessage));
                return false;
            }
        }

        // Apply chat filtering to private messages (if enabled)
        if (plugin.getConfigManager().isPrivateMessageFilterEnabled()) {
            String filteredMessage = plugin.getChatControlManager().applyMessageFilter(message, sender);
            if (filteredMessage == null) {
                // Message was blocked by filter
                String errorMessage = plugin.getConfigManager().getPrivateMessageFilterBlockedMessage();
                sender.sendMessage(MessageUtils.formatMessage(errorMessage));
                return false;
            }
            message = filteredMessage; // Use the filtered message
        }

        // Update reply targets
        setReplyTarget(senderUUID, receiverUUID);
        setReplyTarget(receiverUUID, senderUUID);
        
        // Format and send message to sender
        String senderFormat = plugin.getConfigManager().getMessagingSenderFormat()
                .replace("{receiver}", receiver.getUsername())
                .replace("{message}", message);
        sender.sendMessage(MessageUtils.formatMessage(senderFormat));
        
        // Format and send message to receiver
        String receiverFormat = plugin.getConfigManager().getMessagingReceiverFormat()
                .replace("{sender}", sender.getUsername())
                .replace("{message}", message);
        receiver.sendMessage(MessageUtils.formatMessage(receiverFormat));
        
        // Send to social spies
        broadcastToSocialSpies(sender, receiver, message);
        
        return true;
    }

    /**
     * Attempt to send a cross-proxy private message
     *
     * @param sender The player sending the message
     * @param targetName The name of the target player
     * @param message The message content
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendCrossProxyMessage(Player sender, String targetName, String message) {
        // Check if cross-proxy messaging is available
        if (plugin.getCrossProxyMessagingManager() == null || !plugin.getConfigManager().isPrivateMessagesRedisEnabled()) {
            return false;
        }

        UUID senderUUID = sender.getUniqueId();
        String senderName = sender.getUsername();

        // Check if sender is trying to message themselves
        if (senderName.equalsIgnoreCase(targetName)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorMessageSelf()));
            return false;
        }

        // Attempt cross-proxy message delivery
        plugin.getCrossProxyMessagingManager().sendCrossProxyMessage(senderName, senderUUID, targetName, message)
                .thenAccept(success -> {
                    if (success) {
                        // Message was sent successfully, show confirmation to sender
                        String senderFormat = plugin.getConfigManager().getCrossProxyPrivateMessageSenderFormat()
                                .replace("{receiver}", targetName)
                                .replace("{proxy}", "cross-proxy")
                                .replace("{message}", message);
                        sender.sendMessage(MessageUtils.formatMessage(senderFormat));

                        // Broadcast to local social spies
                        broadcastLocalCrossProxySocialSpy(senderName, senderUUID, targetName, message);

                    } else {
                        // Player not found on any proxy
                        sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getCrossProxyPlayerNotFoundMessage()));
                    }
                })
                .exceptionally(throwable -> {
                    // Error occurred during cross-proxy messaging
                    plugin.getLogger().error("Error during cross-proxy messaging", throwable);
                    sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getCrossProxyLookupTimeoutMessage()));
                    return null;
                });

        return true; // Return true as we've initiated the process
    }
    
    /**
     * Broadcast a private message to all social spies
     *
     * @param sender The sender of the private message
     * @param receiver The receiver of the private message
     * @param message The message content
     */
    private void broadcastToSocialSpies(Player sender, Player receiver, String message) {
        String spyFormat = plugin.getConfigManager().getMessagingSocialSpyFormat()
                .replace("{sender}", sender.getUsername())
                .replace("{receiver}", receiver.getUsername())
                .replace("{message}", message);
        
        Component spyMessage = MessageUtils.formatMessage(spyFormat);
        
        for (Player player : plugin.getServer().getAllPlayers()) {
            UUID playerUUID = player.getUniqueId();
            
            // Don't send to the sender or receiver of the message
            if (playerUUID.equals(sender.getUniqueId()) || playerUUID.equals(receiver.getUniqueId())) {
                continue;
            }
            
            // Only send to players with social spy enabled and the permission
            if (isSocialSpyEnabled(playerUUID) && player.hasPermission("bmsproxycore.socialspy.view")) {
                player.sendMessage(spyMessage);
            }
        }
    }
    
    /**
     * Set the reply target for a player
     *
     * @param player The player's UUID
     * @param target The target's UUID
     */
    public void setReplyTarget(UUID player, UUID target) {
        replyTargets.put(player, target);
    }
    
    /**
     * Get the reply target for a player
     *
     * @param player The player's UUID
     * @return The UUID of the reply target, or null if none exists
     */
    public UUID getReplyTarget(UUID player) {
        return replyTargets.get(player);
    }
    
    /**
     * Remove the reply target for a player
     *
     * @param player The player's UUID
     */
    public void removeReplyTarget(UUID player) {
        replyTargets.remove(player);
    }
    
    /**
     * Toggle social spy status for a player
     *
     * @param player The player's UUID
     * @return The new social spy status
     */
    public boolean toggleSocialSpy(UUID player) {
        if (socialSpyEnabled.contains(player)) {
            socialSpyEnabled.remove(player);
            return false;
        } else {
            socialSpyEnabled.add(player);
            return true;
        }
    }
    
    /**
     * Check if social spy is enabled for a player
     *
     * @param player The player's UUID
     * @return true if social spy is enabled, false otherwise
     */
    public boolean isSocialSpyEnabled(UUID player) {
        return socialSpyEnabled.contains(player);
    }
    
    /**
     * Toggle message acceptance for a player
     *
     * @param player The player's UUID
     * @return The new message toggle status (true = accepting messages, false = not accepting)
     */
    public boolean toggleMessageAcceptance(UUID player) {
        if (messageToggleDisabled.contains(player)) {
            messageToggleDisabled.remove(player);
            return true;
        } else {
            messageToggleDisabled.add(player);
            return false;
        }
    }
    
    /**
     * Check if a player has message receiving disabled
     *
     * @param player The player's UUID
     * @return true if messages are disabled, false otherwise
     */
    public boolean isMessageToggleDisabled(UUID player) {
        return messageToggleDisabled.contains(player);
    }
    
    /**
     * Add a player to another player's ignore list
     *
     * @param player The player doing the ignoring
     * @param ignored The player being ignored
     * @return true if the player was added, false if they were already on the ignore list
     */
    public boolean addIgnoredPlayer(UUID player, UUID ignored) {
        Set<UUID> ignored_set = ignoredPlayers.computeIfAbsent(player, k -> new HashSet<>());
        if (ignored_set.contains(ignored)) {
            return false;
        }
        ignored_set.add(ignored);
        return true;
    }
    
    /**
     * Remove a player from another player's ignore list
     *
     * @param player The player doing the ignoring
     * @param ignored The player being ignored
     * @return true if the player was removed, false if they weren't on the ignore list
     */
    public boolean removeIgnoredPlayer(UUID player, UUID ignored) {
        Set<UUID> ignored_set = ignoredPlayers.get(player);
        if (ignored_set == null || !ignored_set.contains(ignored)) {
            return false;
        }
        ignored_set.remove(ignored);
        return true;
    }
    
    /**
     * Check if a player is ignoring another player
     *
     * @param player The player who might be ignoring
     * @param ignored The player who might be ignored
     * @return true if player is ignoring ignored, false otherwise
     */
    public boolean isPlayerIgnoring(UUID player, UUID ignored) {
        Set<UUID> ignored_set = ignoredPlayers.get(player);
        return ignored_set != null && ignored_set.contains(ignored);
    }
    
    /**
     * Get the list of players a player is ignoring
     *
     * @param player The player
     * @return A Set of UUIDs of ignored players
     */
    public Set<UUID> getIgnoredPlayers(UUID player) {
        return ignoredPlayers.getOrDefault(player, new HashSet<>());
    }
    
    /**
     * Clean up when a player disconnects
     *
     * @param player The player who disconnected
     */
    public void handlePlayerDisconnect(UUID player) {
        // Remove from social spy set
        socialSpyEnabled.remove(player);
        
        // Remove from message toggle set
        messageToggleDisabled.remove(player);
        
        // Remove player as a reply target from all players
        replyTargets.entrySet().removeIf(entry -> entry.getValue().equals(player));
        
        // Remove player's reply target
        replyTargets.remove(player);
    }

    /**
     * Broadcast cross-proxy social spy message to local players only
     */
    private void broadcastLocalCrossProxySocialSpy(String senderName, UUID senderUUID, String receiverName, String message) {
        String socialSpyFormat = plugin.getConfigManager().getCrossProxyPrivateMessageSocialSpyFormat()
                .replace("{sender}", senderName)
                .replace("{sender_proxy}", plugin.getConfigManager().getPrivateMessagesRedisProxyId())
                .replace("{receiver}", receiverName)
                .replace("{receiver_proxy}", "cross-proxy")
                .replace("{message}", message);

        Component formattedMessage = MessageUtils.formatMessage(socialSpyFormat);

        for (Player player : plugin.getServer().getAllPlayers()) {
            if (isSocialSpyEnabled(player.getUniqueId())) {
                // Don't show social spy to the sender
                if (!player.getUniqueId().equals(senderUUID)) {
                    player.sendMessage(formattedMessage);
                }
            }
        }
    }
}