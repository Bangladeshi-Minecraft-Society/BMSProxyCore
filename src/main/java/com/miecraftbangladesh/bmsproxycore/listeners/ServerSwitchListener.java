package com.miecraftbangladesh.bmsproxycore.listeners;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class ServerSwitchListener {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.staffchat.use";

    public ServerSwitchListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onServerSwitch(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        
        // Only handle staff members (those with staff chat permission)
        if (!player.hasPermission(PERMISSION)) {
            return;
        }
        
        // Get previous server info
        Optional<ServerInfo> previousServer = Optional.ofNullable(event.getPreviousServer())
                .map(serverConnection -> serverConnection.getServerInfo());
        
        // Get current server info
        Optional<ServerInfo> currentServer = player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo());
        
        // Both must exist for a server switch (not for initial join)
        if (previousServer.isPresent() && currentServer.isPresent()) {
            String prevServerName = previousServer.get().getName();
            String currentServerName = currentServer.get().getName();
            
            // Format the server switch message
            Component formattedMessage = MessageUtils.formatStaffServerSwitchMessage(
                player,
                prevServerName,
                currentServerName,
                plugin.getConfigManager()
            );
            
            // Broadcast to all staff members
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, PERMISSION);
            
            // Send to Discord webhook if enabled
            plugin.sendStaffServerSwitchMessage(player, prevServerName, currentServerName);
            
            // Log to console
            plugin.getLogger().info(MessageUtils.stripColorCodes(formattedMessage.toString()));
        }
    }
} 