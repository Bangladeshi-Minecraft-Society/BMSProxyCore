package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffChatCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.staffchat.use";

    public StaffChatCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Check if the source is a player or console
        if (!(source instanceof Player) && !(source instanceof ConsoleCommandSource)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player or the console."));
            return;
        }

        // Check permissions for players
        if (source instanceof Player && !source.hasPermission(PERMISSION)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(MessageUtils.formatMessage("&cUsage: /staffchat <message>"));
            return;
        }

        // Join the arguments to form the message
        String message = String.join(" ", args);

        if (source instanceof Player) {
            Player player = (Player) source;
            
            // Format and broadcast the message for a player
            Component formattedMessage = MessageUtils.formatStaffChatMessage(
                player, 
                message, 
                plugin.getConfigManager(), 
                plugin.getServer()
            );
            
            // Broadcast to all staff members
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, PERMISSION);
            
            // Send to Discord webhook if enabled
            plugin.sendStaffChatMessage(player, message);
            
            // Log to console
            plugin.getLogger().info(MessageUtils.stripColorCodes(formattedMessage.toString()));
        } else {
            // It's the console sending a message
            String serverName = "Console";
            String consoleName = "Console";
            
            // Format and broadcast console message
            Component formattedMessage = MessageUtils.formatConsoleStaffChatMessage(
                message,
                plugin.getConfigManager()
            );
            
            // Broadcast to all staff members
            MessageUtils.broadcastToPermission(plugin.getServer(), formattedMessage, PERMISSION);
            
            // Send to Discord if enabled
            plugin.sendConsoleStaffChatMessage(message);
            
            // Log to console (although it's from console already)
            plugin.getLogger().info(MessageUtils.stripColorCodes(formattedMessage.toString()));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Console always has permission
        if (invocation.source() instanceof ConsoleCommandSource) {
            return true;
        }
        return invocation.source().hasPermission(PERMISSION);
    }
} 