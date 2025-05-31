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
 * Command for locking and unlocking chat
 */
public class LockChatCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public LockChatCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        // Check if lock chat is enabled
        if (!plugin.getConfigManager().isLockChatEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlModuleDisabledMessage()));
            return;
        }

        // Check permissions
        String usePermission = plugin.getConfigManager().getLockChatUsePermission();
        if (source instanceof Player && !usePermission.isEmpty() && !source.hasPermission(usePermission)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlNoPermissionMessage()));
            return;
        }

        // Toggle chat lock state
        boolean newLockState = plugin.getChatControlManager().toggleChatLock();
        String playerName = source instanceof Player ? ((Player) source).getUsername() : "Console";

        // Send appropriate message to all players
        String message;
        if (newLockState) {
            // Chat is now locked
            message = plugin.getConfigManager().getLockChatLockedMessage();
        } else {
            // Chat is now unlocked
            message = plugin.getConfigManager().getLockChatUnlockedMessage()
                    .replace("{player}", playerName);
        }

        Component formattedMessage = MessageUtils.formatMessage(message);
        for (Player player : plugin.getServer().getAllPlayers()) {
            player.sendMessage(formattedMessage);
        }

        // Log to console
        plugin.getLogger().info("Chat " + (newLockState ? "locked" : "unlocked") + " by " + playerName);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        // No tab completion needed for lock chat command
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Console always has permission
        if (invocation.source() instanceof ConsoleCommandSource) {
            return true;
        }
        
        String usePermission = plugin.getConfigManager().getLockChatUsePermission();
        return usePermission.isEmpty() || invocation.source().hasPermission(usePermission);
    }
}
