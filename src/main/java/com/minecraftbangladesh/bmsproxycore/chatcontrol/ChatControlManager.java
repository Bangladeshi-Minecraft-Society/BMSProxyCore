package com.minecraftbangladesh.bmsproxycore.chatcontrol;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.velocitypowered.api.proxy.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Manager class for handling chat control functionality including filtering and cooldowns
 */
public class ChatControlManager {

    private final BMSProxyCore plugin;
    
    // Chat filter data
    private final List<Pattern> filterPatterns = new ArrayList<>();
    private final List<String> filterRules = new ArrayList<>();
    
    // Chat cooldown data
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    // Chat lock state
    private volatile boolean chatLocked = false;

    public ChatControlManager(BMSProxyCore plugin) {
        this.plugin = plugin;
        loadFilterRules();
    }

    /**
     * Load filter rules from configuration
     */
    public void loadFilterRules() {
        filterPatterns.clear();
        filterRules.clear();
        
        List<String> rules = plugin.getConfigManager().getChatFilterRules();
        for (String rule : rules) {
            addFilterRule(rule, false); // Don't save to config when loading
        }
        
        if (plugin.getConfigManager().isChatControlDebugEnabled()) {
            plugin.getLogger().info("[ChatControl-Debug] Loaded " + filterRules.size() + " filter rules");
        }
    }

    /**
     * Check if a message should be filtered
     * @param message The message to check
     * @return true if the message should be filtered, false otherwise
     */
    public boolean shouldFilterMessage(String message) {
        if (!plugin.getConfigManager().isChatFilterEnabled()) {
            return false;
        }
        
        for (Pattern pattern : filterPatterns) {
            if (pattern.matcher(message).find()) {
                if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                    plugin.getLogger().info("[ChatControl-Debug] Message filtered by pattern: " + pattern.pattern());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a player can send a message (cooldown check)
     * @param player The player to check
     * @return true if the player can send a message, false if on cooldown
     */
    public boolean canSendMessage(Player player) {
        if (!plugin.getConfigManager().isChatCooldownEnabled()) {
            return true;
        }
        
        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getChatCooldownBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return true;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        Long lastTime = lastMessageTime.get(playerId);
        if (lastTime == null) {
            return true;
        }
        
        int cooldownDuration = getCooldownDuration(player);
        if (cooldownDuration <= 0) {
            return true;
        }
        
        long timeDiff = currentTime - lastTime;
        long cooldownMillis = cooldownDuration * 1000L;
        
        return timeDiff >= cooldownMillis;
    }

    /**
     * Get the remaining cooldown time for a player
     * @param player The player to check
     * @return Remaining cooldown time in seconds, 0 if no cooldown
     */
    public long getRemainingCooldown(Player player) {
        if (!plugin.getConfigManager().isChatCooldownEnabled()) {
            return 0;
        }
        
        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getChatCooldownBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return 0;
        }
        
        UUID playerId = player.getUniqueId();
        Long lastTime = lastMessageTime.get(playerId);
        if (lastTime == null) {
            return 0;
        }
        
        int cooldownDuration = getCooldownDuration(player);
        if (cooldownDuration <= 0) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTime;
        long cooldownMillis = cooldownDuration * 1000L;
        
        if (timeDiff >= cooldownMillis) {
            return 0;
        }
        
        return (cooldownMillis - timeDiff) / 1000L + 1;
    }

    /**
     * Set the last message time for a player
     * @param player The player
     */
    public void setLastMessageTime(Player player) {
        if (plugin.getConfigManager().isChatCooldownEnabled()) {
            lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Get the cooldown duration for a player (considering permission-based cooldowns)
     * @param player The player
     * @return Cooldown duration in seconds
     */
    private int getCooldownDuration(Player player) {
        if (plugin.getConfigManager().isChatCooldownPermissionBasedEnabled()) {
            Map<String, Integer> permissionDurations = plugin.getConfigManager().getChatCooldownPermissionDurations();
            
            // Check permissions in order and return the first match
            for (Map.Entry<String, Integer> entry : permissionDurations.entrySet()) {
                if (player.hasPermission(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        
        return plugin.getConfigManager().getChatCooldownDuration();
    }

    /**
     * Add a new filter rule
     * @param rule The regex pattern to add
     * @param saveToConfig Whether to save the rule to configuration
     * @return true if the rule was added successfully, false otherwise
     */
    public boolean addFilterRule(String rule, boolean saveToConfig) {
        if (filterRules.contains(rule)) {
            return false; // Rule already exists
        }
        
        try {
            Pattern pattern = Pattern.compile(rule, 
                plugin.getConfigManager().isChatFilterCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
            
            filterPatterns.add(pattern);
            filterRules.add(rule);
            
            if (saveToConfig) {
                plugin.getConfigManager().addChatFilterRule(rule);
            }
            
            if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                plugin.getLogger().info("[ChatControl-Debug] Added filter rule: " + rule);
            }
            
            return true;
        } catch (PatternSyntaxException e) {
            if (plugin.getConfigManager().isChatControlDebugEnabled()) {
                plugin.getLogger().warn("[ChatControl-Debug] Invalid regex pattern: " + rule + " - " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Remove a filter rule
     * @param rule The regex pattern to remove
     * @param saveToConfig Whether to save the change to configuration
     * @return true if the rule was removed successfully, false otherwise
     */
    public boolean removeFilterRule(String rule, boolean saveToConfig) {
        int index = filterRules.indexOf(rule);
        if (index == -1) {
            return false; // Rule not found
        }
        
        filterRules.remove(index);
        filterPatterns.remove(index);
        
        if (saveToConfig) {
            plugin.getConfigManager().removeChatFilterRule(rule);
        }
        
        if (plugin.getConfigManager().isChatControlDebugEnabled()) {
            plugin.getLogger().info("[ChatControl-Debug] Removed filter rule: " + rule);
        }
        
        return true;
    }

    /**
     * Get all active filter rules
     * @return List of filter rules
     */
    public List<String> getFilterRules() {
        return new ArrayList<>(filterRules);
    }

    /**
     * Clear all cooldown data
     */
    public void clearCooldowns() {
        lastMessageTime.clear();
        if (plugin.getConfigManager().isChatControlDebugEnabled()) {
            plugin.getLogger().info("[ChatControl-Debug] Cleared all cooldown data");
        }
    }

    /**
     * Get the number of active filter rules
     * @return Number of filter rules
     */
    public int getFilterRuleCount() {
        return filterRules.size();
    }

    /**
     * Check if the maximum number of filter rules has been reached
     * @return true if at maximum, false otherwise
     */
    public boolean isAtMaxFilterRules() {
        return filterRules.size() >= plugin.getConfigManager().getChatControlMaxFilterRules();
    }

    /**
     * Get the number of active cooldown entries
     * @return Number of players with active cooldowns
     */
    public int getActiveCooldownCount() {
        return lastMessageTime.size();
    }

    /**
     * Check if chat is currently locked
     * @return true if chat is locked, false otherwise
     */
    public boolean isChatLocked() {
        return chatLocked;
    }

    /**
     * Set the chat lock state
     * @param locked true to lock chat, false to unlock
     */
    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
        if (plugin.getConfigManager().isChatControlDebugEnabled()) {
            plugin.getLogger().info("[ChatControl-Debug] Chat lock state changed to: " + (locked ? "LOCKED" : "UNLOCKED"));
        }
    }

    /**
     * Toggle the chat lock state
     * @return The new chat lock state (true = locked, false = unlocked)
     */
    public boolean toggleChatLock() {
        chatLocked = !chatLocked;
        if (plugin.getConfigManager().isChatControlDebugEnabled()) {
            plugin.getLogger().info("[ChatControl-Debug] Chat lock toggled to: " + (chatLocked ? "LOCKED" : "UNLOCKED"));
        }
        return chatLocked;
    }

    /**
     * Check if a player can send a message considering chat lock
     * @param player The player to check
     * @return true if the player can send a message, false if blocked by chat lock
     */
    public boolean canSendMessageWithChatLock(Player player) {
        if (!chatLocked) {
            return true;
        }

        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getLockChatBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return true;
        }

        return false;
    }

    /**
     * Apply chat filtering to a message (for use with private messages)
     * @param message The message to filter
     * @param player The player sending the message (for bypass permission check)
     * @return The filtered message, or null if the message should be blocked
     */
    public String applyMessageFilter(String message, Player player) {
        if (!plugin.getConfigManager().isChatFilterEnabled()) {
            return message;
        }

        // Check bypass permission for private messages
        String bypassPermission = plugin.getConfigManager().getChatFilterBypassPrivateMessagesPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return message;
        }

        // Check if message should be filtered
        if (shouldFilterMessage(message)) {
            String action = plugin.getConfigManager().getChatFilterAction();

            switch (action.toLowerCase()) {
                case "block":
                case "warn":
                    // Block the message
                    return null;

                case "replace":
                    // Replace filtered content with replacement text
                    String replacementText = plugin.getConfigManager().getChatFilterReplacementText();
                    return replaceFilteredContent(message, replacementText);

                default:
                    return message;
            }
        }

        return message;
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
        for (String rule : getFilterRules()) {
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
