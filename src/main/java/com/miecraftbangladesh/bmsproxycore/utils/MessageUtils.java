package com.miecraftbangladesh.bmsproxycore.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MessageUtils {

    /**
     * Formats a message with color codes.
     *
     * @param message The message to format
     * @return The formatted message as a Component
     */
    public static Component formatMessage(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Formats a staff chat message using the configured format.
     *
     * @param player The player who sent the message
     * @param message The message content
     * @param configManager The config manager instance
     * @param server The proxy server instance
     * @return The formatted staff chat message as a Component
     */
    public static Component formatStaffChatMessage(Player player, String message, ConfigManager configManager, ProxyServer server) {
        String serverName = player.getCurrentServer().map(serverConnection -> 
                serverConnection.getServerInfo().getName()).orElse("Unknown");
        
        String prefix = configManager.getStaffChatPrefix();
        String format = configManager.getMessageFormat();
        
        format = format.replace("{prefix}", prefix)
                .replace("{player}", player.getUsername())
                .replace("{server}", serverName)
                .replace("{message}", message);
        
        return formatMessage(format);
    }

    /**
     * Formats a staff chat message from the console using the configured format.
     *
     * @param message The message content
     * @param configManager The config manager instance
     * @return The formatted console staff chat message as a Component
     */
    public static Component formatConsoleStaffChatMessage(String message, ConfigManager configManager) {
        String prefix = configManager.getStaffChatPrefix();
        String format = configManager.getMessageFormat();
        
        format = format.replace("{prefix}", prefix)
                .replace("{player}", "Console")
                .replace("{server}", "Console")
                .replace("{message}", message);
        
        return formatMessage(format);
    }

    /**
     * Formats a staff server switch message using the configured format.
     *
     * @param player The player who switched servers
     * @param fromServer The server the player switched from
     * @param toServer The server the player switched to
     * @param configManager The config manager instance
     * @return The formatted staff server switch message as a Component
     */
    public static Component formatStaffServerSwitchMessage(Player player, String fromServer, String toServer, ConfigManager configManager) {
        String prefix = configManager.getStaffChatPrefix();
        String format = configManager.getServerSwitchFormat();
        
        format = format.replace("{prefix}", prefix)
                .replace("{player}", player.getUsername())
                .replace("{from_server}", fromServer)
                .replace("{to_server}", toServer);
        
        return formatMessage(format);
    }
    
    /**
     * Formats a staff connect message using the configured format.
     *
     * @param player The player who connected
     * @param configManager The config manager instance
     * @return The formatted staff connect message as a Component
     */
    public static Component formatStaffConnectMessage(Player player, ConfigManager configManager) {
        String prefix = configManager.getStaffChatPrefix();
        String format = configManager.getConnectFormat();
        String serverName = player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("Unknown");
        
        format = format.replace("{prefix}", prefix)
                .replace("{player}", player.getUsername())
                .replace("{server}", serverName);
        
        return formatMessage(format);
    }
    
    /**
     * Formats a staff disconnect message using the configured format.
     *
     * @param player The player who disconnected
     * @param configManager The config manager instance
     * @return The formatted staff disconnect message as a Component
     */
    public static Component formatStaffDisconnectMessage(Player player, ConfigManager configManager) {
        String prefix = configManager.getStaffChatPrefix();
        String format = configManager.getDisconnectFormat();
        String serverName = player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("Unknown");
        
        format = format.replace("{prefix}", prefix)
                .replace("{player}", player.getUsername())
                .replace("{server}", serverName);
        
        return formatMessage(format);
    }

    /**
     * Strips color codes from a message for console logging
     *
     * @param message The message to strip colors from
     * @return The plain text message without color codes
     */
    public static String stripColorCodes(String message) {
        return message.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Converts a Component to plain text for console logging
     *
     * @param component The component to convert
     * @return The plain text representation
     */
    public static String componentToPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Broadcasts a message to all players with a specific permission.
     *
     * @param server The proxy server instance
     * @param message The message to broadcast
     * @param permission The permission required to receive the message
     */
    public static void broadcastToPermission(ProxyServer server, Component message, String permission) {
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }
} 