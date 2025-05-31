package com.minecraftbangladesh.bmsproxycore.listeners;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class ConnectionListener {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.staffchat.use";

    public ConnectionListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerConnect(PostLoginEvent event) {
        Player player = event.getPlayer();

        // Broadcast player join for cross-proxy tab completion (for all players)
        if (plugin.getCrossProxyMessagingManager() != null && plugin.getConfigManager().isPrivateMessagesRedisEnabled()) {
            plugin.getCrossProxyMessagingManager().broadcastPlayerJoin(player);
        }

        // Only handle staff members for staff chat notifications
        if (!player.hasPermission(PERMISSION)) {
            return;
        }

        // Format the connect message
        Component formattedMessage = MessageUtils.formatStaffConnectMessage(
            player,
            plugin.getConfigManager()
        );

        // Broadcast to all staff members
        MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, PERMISSION);

        // Send to Discord webhook if enabled
        plugin.sendStaffConnectMessage(player);

        // Log to console with proper formatting
        plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));
    }
    
    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        
        // Only handle staff members
        if (!player.hasPermission(PERMISSION)) {
            return;
        }
        
        // Format the disconnect message
        Component formattedMessage = MessageUtils.formatStaffDisconnectMessage(
            player,
            plugin.getConfigManager()
        );
        
        // Broadcast to all staff members
        MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, PERMISSION);
        
        // Send to Discord webhook if enabled
        plugin.sendStaffDisconnectMessage(player);
        
        // Log to console with proper formatting
        plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));
    }
} 