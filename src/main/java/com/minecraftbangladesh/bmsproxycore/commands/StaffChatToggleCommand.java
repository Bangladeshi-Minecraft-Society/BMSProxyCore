package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffChatToggleCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public StaffChatToggleCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        // Check if Staff Chat module is enabled
        if (!plugin.isStaffChatModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getModuleDisabledMessage()));
            return;
        }

        if (!(source instanceof Player)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player."));
            return;
        }

        Player player = (Player) source;

        String togglePermission = plugin.getConfigManager().getStaffChatTogglePermission();
        if (!togglePermission.isEmpty() && !player.hasPermission(togglePermission)) {
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
        String togglePermission = plugin.getConfigManager().getStaffChatTogglePermission();
        return togglePermission.isEmpty() || invocation.source().hasPermission(togglePermission);
    }
}