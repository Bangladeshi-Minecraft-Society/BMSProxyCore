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

    public ServerSwitchListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onServerSwitch(ServerPostConnectEvent event) {
        Player player = event.getPlayer();

        // Check if Staff Chat module is enabled
        if (!plugin.isStaffChatModuleEnabled()) {
            return;
        }

        // Only handle staff members (those with staff activity permission)
        String activityPermission = plugin.getConfigManager().getStaffChatActivityPermission();
        if (!activityPermission.isEmpty() && !player.hasPermission(activityPermission)) {
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

            // Broadcast to all staff members with activity permission
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, activityPermission);

            // Send to Discord webhook if enabled
            plugin.sendStaffServerSwitchMessage(player, prevServerName, currentServerName);

            // Log to console with proper formatting
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));
        }
    }
}