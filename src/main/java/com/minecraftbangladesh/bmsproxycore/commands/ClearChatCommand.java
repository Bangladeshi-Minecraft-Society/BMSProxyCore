package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

/**
 * Command for clearing chat by sending multiple empty messages
 */
public class ClearChatCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public ClearChatCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        // Check if clear chat is enabled
        if (!plugin.getConfigManager().isClearChatEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlModuleDisabledMessage()));
            return;
        }

        // Check permissions
        String usePermission = plugin.getConfigManager().getClearChatUsePermission();
        if (source instanceof Player && !usePermission.isEmpty() && !source.hasPermission(usePermission)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlNoPermissionMessage()));
            return;
        }

        // Get the number of empty messages to send
        int emptyMessages = plugin.getConfigManager().getClearChatEmptyMessages();
        
        // Send empty messages to all players
        Component emptyMessage = Component.empty();
        for (Player player : plugin.getServer().getAllPlayers()) {
            for (int i = 0; i < emptyMessages; i++) {
                player.sendMessage(emptyMessage);
            }
        }

        // Send completion message to all players
        String playerName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        String completionMessage = plugin.getConfigManager().getClearChatCompletionMessage()
                .replace("{player}", playerName);
        
        Component formattedCompletionMessage = MessageUtils.formatMessage(completionMessage);
        for (Player player : plugin.getServer().getAllPlayers()) {
            player.sendMessage(formattedCompletionMessage);
        }

        // Log to console
        plugin.getLogger().info("Chat cleared by " + playerName);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        // No tab completion needed for clear chat command
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Console always has permission
        if (invocation.source() instanceof ConsoleCommandSource) {
            return true;
        }
        
        String usePermission = plugin.getConfigManager().getClearChatUsePermission();
        return usePermission.isEmpty() || invocation.source().hasPermission(usePermission);
    }
}
