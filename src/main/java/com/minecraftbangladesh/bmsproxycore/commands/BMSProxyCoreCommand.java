package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BMSProxyCoreCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String RELOAD_PERMISSION = "bmsproxycore.admin.reload";
    private static final String INFO_PERMISSION = "bmsproxycore.admin.info";

    public BMSProxyCoreCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(source);
                break;
            case "status":
            case "info":
                handleStatus(source);
                break;
            case "modules":
                handleModules(source);
                break;
            default:
                sendHelp(source);
                break;
        }
    }

    private void handleReload(CommandSource source) {
        if (!source.hasPermission(RELOAD_PERMISSION)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        source.sendMessage(MessageUtils.formatMessage("&eReloading BMSProxyCore configuration..."));

        BMSProxyCore.ReloadResult result = plugin.reloadConfiguration();

        if (result.success) {
            source.sendMessage(MessageUtils.formatMessage("&a&lConfiguration Reload Complete!"));

            if (!result.changes.isEmpty()) {
                source.sendMessage(MessageUtils.formatMessage("&6Changes made:"));
                for (String change : result.changes) {
                    source.sendMessage(MessageUtils.formatMessage("&7- " + change));
                }
            }

            // Show module status
            source.sendMessage(MessageUtils.formatMessage("&6Module Status:"));
            source.sendMessage(MessageUtils.formatMessage("&7- Staff Chat: " +
                (plugin.isStaffChatModuleEnabled() ? "&aEnabled" : "&cDisabled")));
            source.sendMessage(MessageUtils.formatMessage("&7- Private Messages: " +
                (plugin.isPrivateMessagesModuleEnabled() ? "&aEnabled" : "&cDisabled")));
            source.sendMessage(MessageUtils.formatMessage("&7- Lobby Command: " +
                (plugin.isLobbyCommandModuleEnabled() ? "&aEnabled" : "&cDisabled")));

        } else {
            source.sendMessage(MessageUtils.formatMessage("&c&lReload Failed!"));
            if (result.error != null) {
                source.sendMessage(MessageUtils.formatMessage("&cError: " + result.error));
            }
        }
    }

    private void handleStatus(CommandSource source) {
        if (!source.hasPermission(INFO_PERMISSION)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        source.sendMessage(MessageUtils.formatMessage("&b&lBMSProxyCore Status"));
        source.sendMessage(MessageUtils.formatMessage("&7Version: &f1.0.0"));
        source.sendMessage(MessageUtils.formatMessage(""));

        // Module status
        source.sendMessage(MessageUtils.formatMessage("&6Module Status:"));

        boolean staffChatEnabled = plugin.isStaffChatModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&7- Staff Chat: " +
            (staffChatEnabled ? "&aEnabled" : "&cDisabled")));

        if (staffChatEnabled) {
            boolean discordEnabled = plugin.getConfigManager().isDiscordEnabled();
            source.sendMessage(MessageUtils.formatMessage("  &7- Discord Integration: " +
                (discordEnabled ? "&aEnabled" : "&cDisabled")));
        }

        boolean privateMessagesEnabled = plugin.isPrivateMessagesModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&7- Private Messages: " +
            (privateMessagesEnabled ? "&aEnabled" : "&cDisabled")));

        boolean lobbyCommandEnabled = plugin.isLobbyCommandModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&7- Lobby Command: " +
            (lobbyCommandEnabled ? "&aEnabled" : "&cDisabled")));

        if (lobbyCommandEnabled) {
            String targetServer = plugin.getConfigManager().getLobbyTargetServer();
            source.sendMessage(MessageUtils.formatMessage("  &7- Target Server: &f" + targetServer));
        }
    }

    private void handleModules(CommandSource source) {
        if (!source.hasPermission(INFO_PERMISSION)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        source.sendMessage(MessageUtils.formatMessage("&b&lBMSProxyCore Modules"));
        source.sendMessage(MessageUtils.formatMessage(""));

        // Staff Chat Module
        boolean staffChatEnabled = plugin.isStaffChatModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&6Staff Chat Module: " +
            (staffChatEnabled ? "&aEnabled" : "&cDisabled")));
        source.sendMessage(MessageUtils.formatMessage("&7- Commands: /staffchat, /staffchattoggle"));
        source.sendMessage(MessageUtils.formatMessage("&7- Features: Staff messaging, Discord integration, Activity tracking"));

        if (staffChatEnabled) {
            boolean discordEnabled = plugin.getConfigManager().isDiscordEnabled();
            source.sendMessage(MessageUtils.formatMessage("&7- Discord: " +
                (discordEnabled ? "&aEnabled" : "&cDisabled")));
        }

        source.sendMessage(MessageUtils.formatMessage(""));

        // Private Messages Module
        boolean privateMessagesEnabled = plugin.isPrivateMessagesModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&6Private Messages Module: " +
            (privateMessagesEnabled ? "&aEnabled" : "&cDisabled")));
        source.sendMessage(MessageUtils.formatMessage("&7- Commands: /msg, /reply, /socialspy, /msgtoggle, /ignore"));
        source.sendMessage(MessageUtils.formatMessage("&7- Features: Private messaging, Social spy, Ignore system"));

        source.sendMessage(MessageUtils.formatMessage(""));

        // Lobby Command Module
        boolean lobbyCommandEnabled = plugin.isLobbyCommandModuleEnabled();
        source.sendMessage(MessageUtils.formatMessage("&6Lobby Command Module: " +
            (lobbyCommandEnabled ? "&aEnabled" : "&cDisabled")));

        if (lobbyCommandEnabled) {
            String mainCommand = plugin.getConfigManager().getLobbyMainCommand();
            java.util.List<String> aliases = plugin.getConfigManager().getLobbyCommandAliases();
            String targetServer = plugin.getConfigManager().getLobbyTargetServer();
            int cooldown = plugin.getConfigManager().getLobbyCooldown();

            source.sendMessage(MessageUtils.formatMessage("&7- Main Command: /" + mainCommand));
            source.sendMessage(MessageUtils.formatMessage("&7- Aliases: " + String.join(", ", aliases.stream().map(alias -> "/" + alias).toArray(String[]::new))));
            source.sendMessage(MessageUtils.formatMessage("&7- Target Server: " + targetServer));
            source.sendMessage(MessageUtils.formatMessage("&7- Cooldown: " + (cooldown > 0 ? cooldown + " seconds" : "Disabled")));
        } else {
            source.sendMessage(MessageUtils.formatMessage("&7- Commands: /lobby, /hub (when enabled)"));
        }
        source.sendMessage(MessageUtils.formatMessage("&7- Features: Server teleportation, Cooldown system"));
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(MessageUtils.formatMessage("&b&lBMSProxyCore &7- &fModular Proxy System"));
        source.sendMessage(MessageUtils.formatMessage("&7Available commands:"));
        source.sendMessage(MessageUtils.formatMessage("&e/bmsproxycore reload &7- Reload configuration and modules"));
        source.sendMessage(MessageUtils.formatMessage("&e/bmsproxycore status &7- Show plugin and module status"));
        source.sendMessage(MessageUtils.formatMessage("&e/bmsproxycore modules &7- Show detailed module information"));
        source.sendMessage(MessageUtils.formatMessage(""));
        source.sendMessage(MessageUtils.formatMessage("&7Module commands (when enabled):"));

        if (plugin.isStaffChatModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage("&a/staffchat <message> &7- Send a message to staff chat"));
            source.sendMessage(MessageUtils.formatMessage("&a/staffchattoggle &7- Toggle staff chat mode"));
        } else {
            source.sendMessage(MessageUtils.formatMessage("&c/staffchat, /staffchattoggle &7- (Staff Chat module disabled)"));
        }

        if (plugin.isPrivateMessagesModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage("&a/msg <player> <message> &7- Send a private message"));
            source.sendMessage(MessageUtils.formatMessage("&a/reply <message> &7- Reply to last message"));
            source.sendMessage(MessageUtils.formatMessage("&a/socialspy &7- Toggle social spy"));
            source.sendMessage(MessageUtils.formatMessage("&a/msgtoggle &7- Toggle message acceptance"));
            source.sendMessage(MessageUtils.formatMessage("&a/ignore <add|remove|list> [player] &7- Manage ignored players"));
        } else {
            source.sendMessage(MessageUtils.formatMessage("&c/msg, /reply, /socialspy, /msgtoggle, /ignore &7- (Private Messages module disabled)"));
        }

        if (plugin.isLobbyCommandModuleEnabled()) {
            String mainCommand = plugin.getConfigManager().getLobbyMainCommand();
            java.util.List<String> aliases = plugin.getConfigManager().getLobbyCommandAliases();
            String commandList = "/" + mainCommand;
            if (!aliases.isEmpty()) {
                commandList += ", /" + String.join(", /", aliases);
            }
            source.sendMessage(MessageUtils.formatMessage("&a" + commandList + " &7- Teleport to lobby server"));
        } else {
            source.sendMessage(MessageUtils.formatMessage("&c/lobby, /hub &7- (Lobby Command module disabled)"));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            return CompletableFuture.completedFuture(List.of("reload", "status", "info", "modules"));
        }

        return CompletableFuture.completedFuture(List.of());
    }
}