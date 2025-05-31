package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class IgnoreCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String PERMISSION = "bmsproxycore.message.ignore";

    public IgnoreCommand(BMSProxyCore plugin) {
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

        Player player = (Player) source;

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageIgnore()));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageIgnore()));
                    return;
                }
                handleAddIgnore(player, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageIgnore()));
                    return;
                }
                handleRemoveIgnore(player, args[1]);
                break;
            case "list":
                handleListIgnore(player);
                break;
            default:
                player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorInvalidUsageIgnore()));
                break;
        }
    }

    private void handleAddIgnore(Player player, String targetName) {
        Optional<Player> targetOptional = plugin.getServer().getPlayer(targetName);
        if (targetOptional.isEmpty()) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorPlayerNotFound()));
            return;
        }

        Player target = targetOptional.get();
        
        // Don't allow ignoring yourself
        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(MessageUtils.formatMessage("&cYou cannot ignore yourself."));
            return;
        }

        // Add the player to the ignore list
        boolean added = plugin.getMessagingManager().addIgnoredPlayer(player.getUniqueId(), target.getUniqueId());
        
        if (added) {
            String message = plugin.getConfigManager().getMessagingIgnoreAdded()
                    .replace("{player}", target.getUsername());
            player.sendMessage(MessageUtils.formatMessage(message));
        } else {
            String message = plugin.getConfigManager().getMessagingErrorAlreadyIgnoring()
                    .replace("{player}", target.getUsername());
            player.sendMessage(MessageUtils.formatMessage(message));
        }
    }

    private void handleRemoveIgnore(Player player, String targetName) {
        Optional<Player> targetOptional = plugin.getServer().getPlayer(targetName);
        if (targetOptional.isEmpty()) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingErrorPlayerNotFound()));
            return;
        }

        Player target = targetOptional.get();

        // Remove the player from the ignore list
        boolean removed = plugin.getMessagingManager().removeIgnoredPlayer(player.getUniqueId(), target.getUniqueId());
        
        if (removed) {
            String message = plugin.getConfigManager().getMessagingIgnoreRemoved()
                    .replace("{player}", target.getUsername());
            player.sendMessage(MessageUtils.formatMessage(message));
        } else {
            String message = plugin.getConfigManager().getMessagingErrorNotIgnoring()
                    .replace("{player}", target.getUsername());
            player.sendMessage(MessageUtils.formatMessage(message));
        }
    }

    private void handleListIgnore(Player player) {
        Set<UUID> ignoredPlayers = plugin.getMessagingManager().getIgnoredPlayers(player.getUniqueId());
        
        if (ignoredPlayers.isEmpty()) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingIgnoreListEmpty()));
            return;
        }
        
        player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessagingIgnoreListHeader()));
        
        for (UUID ignoredUUID : ignoredPlayers) {
            Optional<Player> ignoredPlayer = plugin.getServer().getPlayer(ignoredUUID);
            String name = ignoredPlayer.map(Player::getUsername).orElse("Unknown");
            
            String entry = plugin.getConfigManager().getMessagingIgnoreListEntry()
                    .replace("{player}", name);
            player.sendMessage(MessageUtils.formatMessage(entry));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length == 1) {
            // Sub-commands
            List<String> completions = Arrays.asList("add", "remove", "list");
            String current = args[0].toLowerCase();
            return CompletableFuture.completedFuture(
                    completions.stream()
                    .filter(c -> c.startsWith(current))
                    .collect(Collectors.toList())
            );
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            // Player names for add/remove
            String partialName = args[1].toLowerCase();
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
        return invocation.source().hasPermission(PERMISSION);
    }
} 