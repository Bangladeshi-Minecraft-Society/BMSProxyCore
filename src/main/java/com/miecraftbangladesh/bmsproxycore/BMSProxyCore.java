package com.miecraftbangladesh.bmsproxycore;

import com.google.inject.Inject;
import com.miecraftbangladesh.bmsproxycore.commands.*;
import com.miecraftbangladesh.bmsproxycore.listeners.ChatListener;
import com.miecraftbangladesh.bmsproxycore.listeners.DisconnectListener;
import com.miecraftbangladesh.bmsproxycore.listeners.MessagingDisconnectListener;
import com.miecraftbangladesh.bmsproxycore.messaging.MessagingManager;
import com.miecraftbangladesh.bmsproxycore.utils.ConfigManager;
import com.miecraftbangladesh.bmsproxycore.utils.DiscordWebhook;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Plugin(
        id = "bmsproxycore",
        name = "BMSProxyCore",
        version = "1.0.0",
        description = "A staff chat system for Velocity proxies",
        authors = {"MieCraftBangladesh"}
)
public class BMSProxyCore {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;
    private DiscordWebhook discordWebhook;
    private MessagingManager messagingManager;
    private final Set<UUID> staffChatToggled = new HashSet<>();

    @Inject
    public BMSProxyCore(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize config
        configManager = new ConfigManager(dataDirectory);
        configManager.loadConfig();

        // Initialize Discord webhook
        discordWebhook = new DiscordWebhook(this);
        
        // Initialize messaging manager
        messagingManager = new MessagingManager(this);

        // Register StaffChat commands
        server.getCommandManager().register("staffchat", new StaffChatCommand(this), "sc");
        server.getCommandManager().register("staffchattoggle", new StaffChatToggleCommand(this), "sctoggle");
        server.getCommandManager().register("bmsproxycore", new BMSProxyCoreCommand(this));

        // Register Messaging commands
        server.getCommandManager().register("msg", new MessageCommand(this), "whisper");
        server.getCommandManager().register("reply", new ReplyCommand(this), "r");
        server.getCommandManager().register("socialspy", new SocialSpyCommand(this));
        server.getCommandManager().register("msgtoggle", new MessageToggleCommand(this));
        server.getCommandManager().register("ignore", new IgnoreCommand(this));

        // Register StaffChat listeners
        server.getEventManager().register(this, new ChatListener(this));
        server.getEventManager().register(this, new DisconnectListener(this));
        
        // Register Messaging listeners
        server.getEventManager().register(this, new MessagingDisconnectListener(this));

        logger.info("BMSProxyCore has been enabled!");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }
    
    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    public Set<UUID> getStaffChatToggled() {
        return staffChatToggled;
    }

    public boolean isStaffChatToggled(UUID uuid) {
        return staffChatToggled.contains(uuid);
    }

    public void toggleStaffChat(UUID uuid) {
        if (staffChatToggled.contains(uuid)) {
            staffChatToggled.remove(uuid);
        } else {
            staffChatToggled.add(uuid);
        }
    }

    /**
     * Send a message to staff chat from a player
     * 
     * @param sender The player who sent the message
     * @param message The message content
     */
    public void sendStaffChatMessage(Player sender, String message) {
        // Get the server name
        String serverName = sender.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("Unknown");

        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendStaffChatMessage(sender, message, serverName);
        }
    }
    
    /**
     * Send a message to staff chat from the console
     * 
     * @param message The message content
     */
    public void sendConsoleStaffChatMessage(String message) {
        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendConsoleStaffChatMessage(message);
        }
    }
}