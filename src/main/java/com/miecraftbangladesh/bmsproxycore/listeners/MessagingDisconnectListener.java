package com.miecraftbangladesh.bmsproxycore.listeners;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;

public class MessagingDisconnectListener {

    private final BMSProxyCore plugin;

    public MessagingDisconnectListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        
        // Remove player data from messaging manager
        plugin.getMessagingManager().handlePlayerDisconnect(player.getUniqueId());
    }
} 