package com.miecraftbangladesh.bmsproxycore.listeners;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class ChatListener {

    private final BMSProxyCore plugin;

    public ChatListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if Staff Chat module is enabled
        if (!plugin.isStaffChatModuleEnabled()) {
            return;
        }

        // Check if player has staff chat toggled and has permission
        String usePermission = plugin.getConfigManager().getStaffChatUsePermission();
        if (plugin.isStaffChatToggled(player.getUniqueId()) &&
            (usePermission.isEmpty() || player.hasPermission(usePermission))) {
            event.setResult(PlayerChatEvent.ChatResult.denied());

            // Format and broadcast the message
            Component formattedMessage = MessageUtils.formatStaffChatMessage(
                    player,
                    event.getMessage(),
                    plugin.getConfigManager(),
                    plugin.getServer()
            );

            // Broadcast to all staff members
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, usePermission);

            // Send to Discord webhook if enabled
            plugin.sendStaffChatMessage(player, event.getMessage());

            // Log to console with proper formatting
            plugin.getLogger().info(MessageUtils.componentToPlainText(formattedMessage));
        }
    }
}