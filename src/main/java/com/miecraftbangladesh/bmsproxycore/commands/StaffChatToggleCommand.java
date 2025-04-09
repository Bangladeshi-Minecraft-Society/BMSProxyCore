package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffChatToggleCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.staffchat.toggle";

    public StaffChatToggleCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player."));
            return;
        }

        Player player = (Player) source;

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        // Toggle staff chat mode
        plugin.toggleStaffChat(player.getUniqueId());
        
        // Send appropriate message based on new toggle state
        if (plugin.isStaffChatToggled(player.getUniqueId())) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getToggleOnMessage()));
        } else {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getToggleOffMessage()));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }
} 