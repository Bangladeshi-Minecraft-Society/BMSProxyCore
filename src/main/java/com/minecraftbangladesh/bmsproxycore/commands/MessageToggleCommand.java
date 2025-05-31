package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageToggleCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.message.toggle";

    public MessageToggleCommand(BMSProxyCore plugin) {
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

        // Toggle message acceptance
        boolean accepting = plugin.getMessagingManager().toggleMessageAcceptance(player.getUniqueId());
        
        if (accepting) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingToggleEnabled()));
        } else {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingToggleDisabled()));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }
} 