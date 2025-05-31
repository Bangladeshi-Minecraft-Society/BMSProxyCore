package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MessageCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public MessageCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Check if Private Messages module is enabled
        if (!plugin.isPrivateMessagesModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getModuleDisabledMessage()));
            return;
        }

        if (!(source instanceof Player)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player."));
            return;
        }

        Player sender = (Player) source;

        String sendPermission = plugin.getConfigManager().getPrivateMessagesSendPermission();
        if (!sendPermission.isEmpty() && !sender.hasPermission(sendPermission)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageMsg()));
            return;
        }

        String targetName = args[0];
        Optional<Player> targetOptional = plugin.getServer().getPlayer(targetName);

        if (targetOptional.isEmpty()) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorPlayerNotFound()));
            return;
        }

        Player target = targetOptional.get();

        // Build the message from the remaining arguments
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        // Send the message
        plugin.getMessagingManager().sendMessage(sender, target, message);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            // Suggest online player names for the first argument
            String partialName = args[0].toLowerCase();
            List<String> completions = plugin.getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());

            return CompletableFuture.completedFuture(completions);
        }

        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        String sendPermission = plugin.getConfigManager().getPrivateMessagesSendPermission();
        return sendPermission.isEmpty() || invocation.source().hasPermission(sendPermission);
    }
}