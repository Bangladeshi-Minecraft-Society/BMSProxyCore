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
 * Command for managing chat filter rules
 */
public class ChatFilterCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public ChatFilterCommand(BMSProxyCore plugin) {
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

        // Check if Chat Filter component is enabled
        if (!plugin.getConfigManager().isChatFilterEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlModuleDisabledMessage()));
            return;
        }

        // Check permissions
        String managePermission = plugin.getConfigManager().getChatFilterManagePermission();
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
            case "add":
                handleAddCommand(source, args);
                break;
            case "remove":
            case "delete":
                handleRemoveCommand(source, args);
                break;
            case "list":
                handleListCommand(source);
                break;
            case "reload":
                handleReloadCommand(source);
                break;
            case "clear":
                handleClearCommand(source);
                break;
            default:
                sendUsage(source);
                break;
        }
    }

    private void handleAddCommand(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(MessageUtils.formatMessage("&cUsage: /chatfilter add <regex_pattern>"));
            return;
        }

        // Check if we're at the maximum number of rules
        if (plugin.getChatControlManager().isAtMaxFilterRules()) {
            int maxRules = plugin.getConfigManager().getChatControlMaxFilterRules();
            source.sendMessage(MessageUtils.formatMessage("&cMaximum number of filter rules reached (" + maxRules + ")."));
            return;
        }

        // Join all arguments after "add" to form the regex pattern
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) patternBuilder.append(" ");
            patternBuilder.append(args[i]);
        }
        String pattern = patternBuilder.toString();

        if (plugin.getChatControlManager().addFilterRule(pattern, true)) {
            String message = plugin.getConfigManager().getChatFilterRuleAddedMessage()
                    .replace("{rule}", pattern);
            source.sendMessage(MessageUtils.formatMessage(message));
        } else {
            // Check if it already exists or is invalid
            if (plugin.getChatControlManager().getFilterRules().contains(pattern)) {
                String message = plugin.getConfigManager().getChatFilterRuleAlreadyExistsMessage()
                        .replace("{rule}", pattern);
                source.sendMessage(MessageUtils.formatMessage(message));
            } else {
                String message = plugin.getConfigManager().getChatFilterInvalidRegexMessage()
                        .replace("{rule}", pattern);
                source.sendMessage(MessageUtils.formatMessage(message));
            }
        }
    }

    private void handleRemoveCommand(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(MessageUtils.formatMessage("&cUsage: /chatfilter remove <regex_pattern>"));
            return;
        }

        // Join all arguments after "remove" to form the regex pattern
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) patternBuilder.append(" ");
            patternBuilder.append(args[i]);
        }
        String pattern = patternBuilder.toString();

        if (plugin.getChatControlManager().removeFilterRule(pattern, true)) {
            String message = plugin.getConfigManager().getChatFilterRuleRemovedMessage()
                    .replace("{rule}", pattern);
            source.sendMessage(MessageUtils.formatMessage(message));
        } else {
            String message = plugin.getConfigManager().getChatFilterRuleNotFoundMessage()
                    .replace("{rule}", pattern);
            source.sendMessage(MessageUtils.formatMessage(message));
        }
    }

    private void handleListCommand(CommandSource source) {
        List<String> rules = plugin.getChatControlManager().getFilterRules();
        
        if (rules.isEmpty()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatFilterListEmptyMessage()));
            return;
        }

        source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatFilterListHeaderMessage()));
        
        String listFormat = plugin.getConfigManager().getChatFilterListFormatMessage();
        for (String rule : rules) {
            String formattedRule = listFormat.replace("{rule}", rule);
            source.sendMessage(MessageUtils.formatMessage(formattedRule));
        }
    }

    private void handleReloadCommand(CommandSource source) {
        // Check reload permission
        String reloadPermission = plugin.getConfigManager().getChatFilterReloadPermission();
        if (source instanceof Player && !reloadPermission.isEmpty() && !source.hasPermission(reloadPermission)) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlNoPermissionMessage()));
            return;
        }

        try {
            plugin.getChatControlManager().loadFilterRules();
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlReloadSuccessMessage()));
        } catch (Exception e) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getChatControlReloadFailedMessage()));
            plugin.getLogger().error("Failed to reload chat filter configuration", e);
        }
    }

    private void handleClearCommand(CommandSource source) {
        // Confirmation required for clear command
        plugin.getChatControlManager().getFilterRules().clear();
        // Note: This doesn't save to config, it's a temporary clear
        source.sendMessage(MessageUtils.formatMessage("&aAll filter rules have been temporarily cleared. Use '/chatfilter reload' to restore from config."));
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(MessageUtils.formatMessage("&6Chat Filter Commands:"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatfilter add <pattern> &8- &fAdd a new filter rule"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatfilter remove <pattern> &8- &fRemove a filter rule"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatfilter list &8- &fList all active filter rules"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatfilter reload &8- &fReload filter configuration"));
        source.sendMessage(MessageUtils.formatMessage("&7/chatfilter clear &8- &fTemporarily clear all rules"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            String input = args[0].toLowerCase();
            List<String> subCommands = List.of("add", "remove", "list", "reload", "clear");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    suggestions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            // Suggest existing filter rules for removal
            String input = args[1].toLowerCase();
            for (String rule : plugin.getChatControlManager().getFilterRules()) {
                if (rule.toLowerCase().startsWith(input)) {
                    suggestions.add(rule);
                }
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
        
        String managePermission = plugin.getConfigManager().getChatFilterManagePermission();
        return managePermission.isEmpty() || invocation.source().hasPermission(managePermission);
    }
}
