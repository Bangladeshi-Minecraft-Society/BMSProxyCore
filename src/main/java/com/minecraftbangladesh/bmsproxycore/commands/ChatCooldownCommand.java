package com.minecraftbangladesh.bmsproxycore.commands;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.minecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for managing chat cooldown settings
 */
public class ChatCooldownCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public ChatCooldownCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Check if Chat Control module is enabled
        if (!plugin.isChatControlModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlModuleDisabledMessage()));
            return;
        }

        // Check if Chat Cooldown component is enabled
        if (!plugin.getConfigManager().isChatCooldownEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlModuleDisabledMessage()));
            return;
        }

        // Check permissions
        String managePermission = plugin.getConfigManager().getChatCooldownManagePermission();
        if (source instanceof Player && !managePermission.isEmpty() && !source.hasPermission(managePermission)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlNoPermissionMessage()));
            return;
        }

        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                handleSetCommand(source, args);
                break;
            case "status":
            case "info":
                handleStatusCommand(source);
                break;
            case "clear":
                handleClearCommand(source);
                break;
            case "reload":
                handleReloadCommand(source);
                break;
            case "check":
                handleCheckCommand(source, args);
                break;
            default:
                sendUsage(source);
                break;
        }
    }

    private void handleSetCommand(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(MessageUtils.formatMessage("&cUsage: /chatcooldown set <duration_in_seconds>"));
            return;
        }

        try {
            int duration = Integer.parseInt(args[1]);
            
            if (duration < 0) {
                source.sendMessage(MessageUtils.formatMessage("&cDuration cannot be negative."));
                return;
            }

            // Update the configuration
            plugin.getConfigManager().setChatCooldownDuration(duration);
            
            if (duration == 0) {
                source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatCooldownDurationDisabledMessage()));
            } else {
                String message = plugin.getConfigManager().getChatCooldownDurationSetMessage()
                        .replace("{duration}", String.valueOf(duration));
                source.sendMessage(MessageUtils.formatMessage(message));
            }
            
        } catch (NumberFormatException e) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatCooldownInvalidDurationMessage()));
        }
    }

    private void handleStatusCommand(CommandSource source) {
        int duration = plugin.getConfigManager().getChatCooldownDuration();
        
        if (duration <= 0) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatCooldownStatusDisabledMessage()));
        } else {
            String message = plugin.getConfigManager().getChatCooldownStatusEnabledMessage()
                    .replace("{duration}", String.valueOf(duration));
            source.sendMessage(MessageUtils.formatMessage(message));
        }
        
        // Show permission-based cooldowns if enabled
        if (plugin.getConfigManager().isChatCooldownPermissionBasedEnabled()) {
            source.sendMessage(MessageUtils.formatMessage("&7Permission-based cooldowns are enabled:"));
            plugin.getConfigManager().getChatCooldownPermissionDurations().forEach((permission, permDuration) -> {
                source.sendMessage(MessageUtils.formatMessage("&7- " + permission + ": " + permDuration + "s"));
            });
        }
        
        // Show statistics
        source.sendMessage(MessageUtils.formatMessage("&7Active cooldown entries: " + plugin.getChatControlManager().getActiveCooldownCount()));
    }

    private void handleClearCommand(CommandSource source) {
        plugin.getChatControlManager().clearCooldowns();
        source.sendMessage(MessageUtils.formatMessage("&aAll active cooldowns have been cleared."));
    }

    private void handleReloadCommand(CommandSource source) {
        // Check reload permission
        String reloadPermission = plugin.getConfigManager().getChatCooldownReloadPermission();
        if (source instanceof Player && !reloadPermission.isEmpty() && !source.hasPermission(reloadPermission)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlNoPermissionMessage()));
            return;
        }

        try {
            // Reload the configuration
            plugin.getConfigManager().loadConfig();
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlReloadSuccessMessage()));
        } catch (Exception e) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlReloadFailedMessage()));
            plugin.getLogger().error("Failed to reload chat cooldown configuration", e);
        }
    }

    private void handleCheckCommand(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(MessageUtils.formatMessage("&cUsage: /chatcooldown check <player>"));
            return;
        }

        String playerName = args[1];
        Player targetPlayer = plugin.getServer().getPlayer(playerName).orElse(null);
        
        if (targetPlayer == null) {
            source.sendMessage(MessageUtils.formatMessage("&cPlayer not found: " + playerName));
            return;
        }

        long remainingTime = plugin.getChatControlManager().getRemainingCooldown(targetPlayer);
        
        if (remainingTime <= 0) {
            source.sendMessage(MessageUtils.formatMessage("&a" + playerName + " is not on cooldown."));
        } else {
            source.sendMessage(MessageUtils.formatMessage("&e" + playerName + " has " + remainingTime + " seconds remaining on cooldown."));
        }
        
        // Show bypass status
        String bypassPermission = plugin.getConfigManager().getChatCooldownBypassPermission();
        if (!bypassPermission.isEmpty() && targetPlayer.hasPermission(bypassPermission)) {
            source.sendMessage(MessageUtils.formatMessage("&7" + playerName + " has cooldown bypass permission."));
        }
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(MessageUtils.formatMessage("&6Chat Cooldown Commands:"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatcooldown set <seconds> &8- &fSet cooldown duration"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatcooldown status &8- &fShow current cooldown settings"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatcooldown clear &8- &fClear all active cooldowns"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatcooldown check <player> &8- &fCheck a player's cooldown status"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatcooldown reload &8- &fReload cooldown configuration"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            String input = args[0].toLowerCase();
            List<String> subCommands = List.of("set", "status", "clear", "reload", "check");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    suggestions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                // Suggest common durations
                String input = args[1];
                List<String> durations = List.of("0", "1", "2", "3", "5", "10", "15", "30");
                for (String duration : durations) {
                    if (duration.startsWith(input)) {
                        suggestions.add(duration);
                    }
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                // Suggest online player names
                String input = args[1].toLowerCase();
                plugin.getServer().getAllPlayers().forEach(player -> {
                    if (player.getUsername().toLowerCase().startsWith(input)) {
                        suggestions.add(player.getUsername());
                    }
                });
            }
        }

        return CompletableFuture.completedFuture(suggestions);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Console always has permission
        if (invocation.source() instanceof ConsoleCommandSource) {
            return true;
        }
        
        String managePermission = plugin.getConfigManager().getChatCooldownManagePermission();
        return managePermission.isEmpty() || invocation.source().hasPermission(managePermission);
    }
}
