package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnnouncementCommand implements SimpleCommand {

    private final BMSProxyCore plugin;

    public AnnouncementCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Check if Announcement module is enabled
        if (!plugin.isAnnouncementModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getModuleDisabledMessage()));
            return;
        }

        // Check if announcement message is provided
        if (args.length == 0) {
            String usageMessage;
            if (source instanceof ConsoleCommandSource) {
                usageMessage = plugin.getConfigManager().getAnnouncementConsoleUsageMessage();
            } else {
                usageMessage = plugin.getConfigManager().getAnnouncementUsageMessage();
            }
            source.sendMessage(MessageUtils.formatMessage(usageMessage.replace("{command}", "announce")));
            return;
        }

        // Check permission for players (console always has permission)
        if (source instanceof Player) {
            Player player = (Player) source;
            String permission = plugin.getConfigManager().getAnnouncementSendPermission();
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getAnnouncementNoPermissionMessage()));
                return;
            }
        }

        // Join all arguments to form the announcement message
        String announcement = String.join(" ", args);
        
        if (announcement.trim().isEmpty()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getAnnouncementEmptyMessage()));
            return;
        }

        // Send the announcement
        sendAnnouncement(source, announcement);

        // Send success message to sender
        source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getAnnouncementSuccessMessage()));
    }

    private void sendAnnouncement(CommandSource sender, String announcement) {
        // Determine sender information
        String senderName = "Console";
        String senderServer = "Console";
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            senderName = player.getUsername();
            senderServer = player.getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("Unknown");
        }

        // Send chat message if enabled
        if (plugin.getConfigManager().isAnnouncementChatMessageEnabled()) {
            Component chatMessage = createChatMessage(sender, senderName, senderServer, announcement);
            
            // Broadcast to all players
            for (Player player : plugin.getServer().getAllPlayers()) {
                player.sendMessage(chatMessage);
            }
            
            // Log to console
            plugin.getLogger().info(MessageUtils.componentToPlainText(chatMessage));
        }

        // Send title if enabled
        if (plugin.getConfigManager().isAnnouncementTitleEnabled()) {
            Title title = createTitle(announcement);
            
            // Send title to all players
            for (Player player : plugin.getServer().getAllPlayers()) {
                player.showTitle(title);
            }
        }
    }

    private Component createChatMessage(CommandSource sender, String senderName, String senderServer, String announcement) {
        String format;
        
        if (sender instanceof ConsoleCommandSource) {
            format = plugin.getConfigManager().getAnnouncementConsoleFormat();
        } else if (plugin.getConfigManager().isAnnouncementShowSender()) {
            format = plugin.getConfigManager().getAnnouncementSenderFormat()
                    .replace("{sender}", senderName)
                    .replace("{server}", senderServer);
        } else {
            format = plugin.getConfigManager().getAnnouncementChatMessageFormat();
        }
        
        format = format.replace("{announcement}", announcement);
        return MessageUtils.formatMessage(format);
    }

    private Title createTitle(String announcement) {
        // Get title configuration
        String mainTitleText = plugin.getConfigManager().getAnnouncementTitleMainTitle();
        String subtitleText = plugin.getConfigManager().getAnnouncementTitleSubtitle()
                .replace("{announcement}", announcement);
        
        int fadeInTicks = plugin.getConfigManager().getAnnouncementTitleFadeIn();
        int stayTicks = plugin.getConfigManager().getAnnouncementTitleStay();
        int fadeOutTicks = plugin.getConfigManager().getAnnouncementTitleFadeOut();
        
        // Convert ticks to duration (20 ticks = 1 second)
        Duration fadeIn = Duration.ofMillis(fadeInTicks * 50L); // 50ms per tick
        Duration stay = Duration.ofMillis(stayTicks * 50L);
        Duration fadeOut = Duration.ofMillis(fadeOutTicks * 50L);
        
        // Create title components
        Component mainTitle = MessageUtils.formatMessage(mainTitleText);
        Component subtitle = MessageUtils.formatMessage(subtitleText);
        
        // Create and return title
        return Title.title(mainTitle, subtitle, Title.Times.times(fadeIn, stay, fadeOut));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        // No tab completion for announcement messages
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Console always has permission
        if (invocation.source() instanceof ConsoleCommandSource) {
            return true;
        }
        
        // Check player permission
        if (invocation.source() instanceof Player) {
            String permission = plugin.getConfigManager().getAnnouncementSendPermission();
            if (permission.isEmpty()) {
                return true; // No permission required
            }
            return invocation.source().hasPermission(permission);
        }
        
        return false;
    }
}
