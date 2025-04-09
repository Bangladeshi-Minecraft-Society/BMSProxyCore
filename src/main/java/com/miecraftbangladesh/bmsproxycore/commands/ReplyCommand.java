package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReplyCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.message.reply";

    public ReplyCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player."));
            return;
        }

        Player sender = (Player) source;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageReply()));
            return;
        }

        // Get the last player they messaged
        UUID replyTargetUUID = plugin.getMessagingManager().getReplyTarget(sender.getUniqueId());
        
        if (replyTargetUUID == null) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorNoReplyTarget()));
            return;
        }

        Optional<Player> targetOptional = plugin.getServer().getPlayer(replyTargetUUID);
        
        if (targetOptional.isEmpty()) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorPlayerNotFound()));
            return;
        }

        Player target = targetOptional.get();
        
        // Build the message from all arguments
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        // Send the message
        plugin.getMessagingManager().sendMessage(sender, target, message);
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