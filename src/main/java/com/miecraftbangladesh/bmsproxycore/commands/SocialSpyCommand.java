package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SocialSpyCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String TOGGLE_PERMISSION = "bmsproxycore.socialspy.toggle";
    private static final String VIEW_PERMISSION = "bmsproxycore.socialspy.view";

    public SocialSpyCommand(BMSProxyCore plugin) {
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

        if (!player.hasPermission(TOGGLE_PERMISSION)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        if (!player.hasPermission(VIEW_PERMISSION)) {
            player.sendMessage(MessageUtils.formatMessage("&cYou need the permission to view social spy messages."));
            return;
        }

        // Toggle social spy status
        boolean enabled = plugin.getMessagingManager().toggleSocialSpy(player.getUniqueId());
        
        if (enabled) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingSocialSpyEnabled()));
        } else {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingSocialSpyDisabled()));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(TOGGLE_PERMISSION);
    }
} 