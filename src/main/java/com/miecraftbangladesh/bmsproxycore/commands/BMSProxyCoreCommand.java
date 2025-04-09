package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BMSProxyCoreCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private static final String RELOAD_PERMISSION = "bmsproxycore.staffchat.reload";

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

        if (args[0].equalsIgnoreCase("reload")) {
            if (!source.hasPermission(RELOAD_PERMISSION)) {
                source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getNoPermissionMessage()));
                return;
            }

            try {
                plugin.getConfigManager().loadConfig();
                source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getReloadSuccessMessage()));
            } catch (Exception e) {
                source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getReloadFailMessage()));
                plugin.getLogger().error("Failed to reload configuration", e);
            }
        } else {
            sendHelp(source);
        }
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(MessageUtils.formatMessage("&b&lBMSProxyCore &7- &fStaff Chat System"));
        source.sendMessage(MessageUtils.formatMessage("&7/bmsproxycore reload &f- Reload the configuration"));
        source.sendMessage(MessageUtils.formatMessage("&7/staffchat <message> &f- Send a message to staff chat"));
        source.sendMessage(MessageUtils.formatMessage("&7/staffchattoggle &f- Toggle staff chat mode"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length == 1) {
            return CompletableFuture.completedFuture(List.of("reload"));
        }
        
        return CompletableFuture.completedFuture(List.of());
    }
} 