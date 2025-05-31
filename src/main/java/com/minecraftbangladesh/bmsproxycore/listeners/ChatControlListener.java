package com.minecraftbangladesh.bmsproxycore.listeners;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

/**
 * Listener for handling chat control events (filtering and cooldowns)
 */
public class ChatControlListener {

    private final BMSProxyCore plugin;

    public ChatControlListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if Chat Control module is enabled
        if (!plugin.isChatControlModuleEnabled()) {
            return;
        }

        // Skip if player has staff chat toggled (to avoid interfering with staff chat)
        if (plugin.isStaffChatModuleEnabled() && plugin.isStaffChatToggled(player.getUniqueId())) {
            return;
        }

        // Check chat cooldown first
        if (plugin.getConfigManager().isChatCooldownEnabled()) {
            if (!checkCooldown(player, event)) {
                return; // Event already cancelled by cooldown check
            }
        }

        // Check chat filter
        if (plugin.getConfigManager().isChatFilterEnabled()) {
            if (!checkFilter(player, message, event)) {
                return; // Event already cancelled by filter check
            }
        }

        // If we reach here, the message passed all checks
        // Set the last message time for cooldown tracking
        if (plugin.getConfigManager().isChatCooldownEnabled()) {
            plugin.getChatControlManager().setLastMessageTime(player);
        }
    }

    /**
     * Check if the player is on cooldown
     * @param player The player sending the message
     * @param event The chat event
     * @return true if the message should continue processing, false if blocked
     */
    @SuppressWarnings("deprecation") // setResult is deprecated but still the only way to modify chat in Velocity
    private boolean checkCooldown(Player player, PlayerChatEvent event) {
        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getChatCooldownBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return true;
        }

        if (!plugin.getChatControlManager().canSendMessage(player)) {
            // Player is on cooldown
            event.setResult(PlayerChatEvent.ChatResult.denied());
            
            long remainingTime = plugin.getChatControlManager().getRemainingCooldown(player);
            String cooldownMessage = plugin.getConfigManager().getChatCooldownMessage()
                    .replace("{time}", String.valueOf(remainingTime));
            
            player.sendMessage(MessageUtils.formatMessage(cooldownMessage));
            
            // Log cooldown violation if enabled
            if (plugin.getConfigManager().isChatCooldownLogViolationsEnabled()) {
                String logMessage = plugin.getConfigManager().getChatCooldownViolationLogFormat()
                        .replace("{player}", player.getUsername());
                plugin.getLogger().info(logMessage);
            }
            
            // Debug logging
            if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                String debugMessage = "Player " + player.getUsername() + " blocked by cooldown (" + remainingTime + "s remaining)";
                String logFormat = plugin.getConfigManager().getChatControlDebugFormat()
                        .replace("{component}", "Cooldown")
                        .replace("{message}", debugMessage);
                plugin.getLogger().info(logFormat);
            }
            
            return false;
        }
        
        return true;
    }

    /**
     * Check if the message should be filtered
     * @param player The player sending the message
     * @param message The message content
     * @param event The chat event
     * @return true if the message should continue processing, false if blocked
     */
    @SuppressWarnings("deprecation") // setResult is deprecated but still the only way to modify chat in Velocity
    private boolean checkFilter(Player player, String message, PlayerChatEvent event) {
        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getChatFilterBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return true;
        }

        if (plugin.getChatControlManager().shouldFilterMessage(message)) {
            // Message should be filtered
            String action = plugin.getConfigManager().getChatFilterAction();
            
            switch (action.toLowerCase()) {
                case "block":
                    // Silently block the message
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    break;
                    
                case "warn":
                    // Block the message and warn the player
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    String warningMessage = plugin.getConfigManager().getChatFilterBlockedMessage();
                    player.sendMessage(MessageUtils.formatMessage(warningMessage));
                    break;
                    
                case "replace":
                    // Replace filtered content with replacement text
                    String replacementText = plugin.getConfigManager().getChatFilterReplacementText();
                    String filteredMessage = replaceFilteredContent(message, replacementText);
                    event.setResult(PlayerChatEvent.ChatResult.message(filteredMessage));
                    break;
                    
                default:
                    // Default to warn if action is not recognized
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatFilterBlockedMessage()));
                    break;
            }
            
            // Log filtered message if enabled
            if (plugin.getConfigManager().isChatFilterLogEnabled()) {
                String logMessage = plugin.getConfigManager().getChatFilterLogFormat()
                        .replace("{player}", player.getUsername())
                        .replace("{message}", message);
                plugin.getLogger().info(logMessage);
            }
            
            // Debug logging
            if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                String debugMessage = "Message from " + player.getUsername() + " filtered: " + message;
                String logFormat = plugin.getConfigManager().getChatControlDebugFormat()
                        .replace("{component}", "Filter")
                        .replace("{message}", debugMessage);
                plugin.getLogger().info(logFormat);
            }
            
            return false;
        }
        
        return true;
    }

    /**
     * Replace filtered content in a message with replacement text
     * @param message The original message
     * @param replacementText The text to replace filtered content with
     * @return The message with filtered content replaced
     */
    private String replaceFilteredContent(String message, String replacementText) {
        String result = message;
        
        // Get filter rules and apply replacements
        for (String rule : plugin.getChatControlManager().getFilterRules()) {
            try {
                // Apply case sensitivity based on configuration
                String pattern = plugin.getConfigManager().isChatFilterCaseSensitive() ? rule : "(?i)" + rule;
                result = result.replaceAll(pattern, replacementText);
            } catch (Exception e) {
                // If regex replacement fails, continue with next rule
                if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                    plugin.getLogger().warn("[ChatControl-Debug] Failed to apply replacement for rule: " + rule);
                }
            }
        }
        
        return result;
    }
}
