package com.minecraftbangladesh.bmsproxycore.listeners;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;

public class DisconnectListener {

    private final BMSProxyCore plugin;

    public DisconnectListener(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        
        // Remove player from staff chat toggle list when they disconnect
        if (plugin.isStaffChatToggled(player.getUniqueId())) {
            plugin.getStaffChatToggled().remove(player.getUniqueId());
        }
    }
} 