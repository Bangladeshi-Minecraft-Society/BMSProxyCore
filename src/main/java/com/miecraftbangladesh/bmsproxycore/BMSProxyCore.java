package com.miecraftbangladesh.bmsproxycore;

import com.google.inject.Inject;
import com.miecraftbangladesh.bmsproxycore.commands.*;
import com.miecraftbangladesh.bmsproxycore.listeners.*;
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

        // Always register the main admin command
        server.getCommandManager().register("bmsproxycore", new BMSProxyCoreCommand(this));

        // Initialize modules based on configuration
        initializeStaffChatModule();
        initializePrivateMessagesModule();
        initializeLobbyCommandModule();
        initializeAnnouncementModule();

        logger.info("BMSProxyCore has been enabled!");
    }

    private void initializeStaffChatModule() {
        if (!configManager.isStaffChatEnabled()) {
            logger.info("Staff Chat module is disabled in configuration.");
            return;
        }

        logger.info("Initializing Staff Chat module...");

        // Initialize Discord webhook
        discordWebhook = new DiscordWebhook(this);

        // Register StaffChat commands
        server.getCommandManager().register("staffchat", new StaffChatCommand(this), "sc");
        server.getCommandManager().register("staffchattoggle", new StaffChatToggleCommand(this), "sctoggle");

        // Register StaffChat listeners
        server.getEventManager().register(this, new ChatListener(this));
        server.getEventManager().register(this, new DisconnectListener(this));

        // Register staff activity listeners
        server.getEventManager().register(this, new ServerSwitchListener(this));
        server.getEventManager().register(this, new ConnectionListener(this));

        logger.info("Staff Chat module initialized successfully.");
    }

    private void initializePrivateMessagesModule() {
        if (!configManager.isPrivateMessagesEnabled()) {
            logger.info("Private Messages module is disabled in configuration.");
            return;
        }

        logger.info("Initializing Private Messages module...");

        // Initialize messaging manager
        messagingManager = new MessagingManager(this);

        // Register Messaging commands
        server.getCommandManager().register("msg", new MessageCommand(this), "whisper");
        server.getCommandManager().register("reply", new ReplyCommand(this), "r");
        server.getCommandManager().register("socialspy", new SocialSpyCommand(this));
        server.getCommandManager().register("msgtoggle", new MessageToggleCommand(this));
        server.getCommandManager().register("ignore", new IgnoreCommand(this));

        // Register Messaging listeners
        server.getEventManager().register(this, new MessagingDisconnectListener(this));

        logger.info("Private Messages module initialized successfully.");
    }

    private void initializeLobbyCommandModule() {
        if (!configManager.isLobbyCommandEnabled()) {
            logger.info("Lobby Command module is disabled in configuration.");
            return;
        }

        logger.info("Initializing Lobby Command module...");

        // Get command configuration
        String mainCommand = configManager.getLobbyMainCommand();
        java.util.List<String> aliases = configManager.getLobbyCommandAliases();

        // Register lobby command with aliases
        LobbyCommand lobbyCommand = new LobbyCommand(this);
        server.getCommandManager().register(mainCommand, lobbyCommand, aliases.toArray(new String[0]));

        logger.info("Lobby Command module initialized successfully.");
        logger.info("Registered command: /" + mainCommand + " with aliases: " + aliases);
    }

    private void initializeAnnouncementModule() {
        if (!configManager.isAnnouncementEnabled()) {
            logger.info("Announcement module is disabled in configuration.");
            return;
        }

        logger.info("Initializing Announcement module...");

        // Get command configuration
        String mainCommand = configManager.getAnnouncementMainCommand();
        java.util.List<String> aliases = configManager.getAnnouncementCommandAliases();

        // Register announcement command with aliases
        AnnouncementCommand announcementCommand = new AnnouncementCommand(this);
        server.getCommandManager().register(mainCommand, announcementCommand, aliases.toArray(new String[0]));

        logger.info("Announcement module initialized successfully.");
        logger.info("Registered command: /" + mainCommand + " with aliases: " + aliases);
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

    public boolean isStaffChatModuleEnabled() {
        return configManager.isStaffChatEnabled();
    }

    public boolean isPrivateMessagesModuleEnabled() {
        return configManager.isPrivateMessagesEnabled();
    }

    public boolean isLobbyCommandModuleEnabled() {
        return configManager.isLobbyCommandEnabled();
    }

    public boolean isAnnouncementModuleEnabled() {
        return configManager.isAnnouncementEnabled();
    }

    /**
     * Reload configuration and reinitialize modules as needed
     * @return ReloadResult containing information about what changed
     */
    public ReloadResult reloadConfiguration() {
        ReloadResult result = new ReloadResult();

        try {
            // Store current module states
            boolean wasStaffChatEnabled = configManager.isStaffChatEnabled();
            boolean wasPrivateMessagesEnabled = configManager.isPrivateMessagesEnabled();
            boolean wasLobbyCommandEnabled = configManager.isLobbyCommandEnabled();
            boolean wasAnnouncementEnabled = configManager.isAnnouncementEnabled();

            // Reload configuration
            configManager.loadConfig();
            result.configReloaded = true;

            // Check for module state changes
            boolean isStaffChatEnabled = configManager.isStaffChatEnabled();
            boolean isPrivateMessagesEnabled = configManager.isPrivateMessagesEnabled();
            boolean isLobbyCommandEnabled = configManager.isLobbyCommandEnabled();
            boolean isAnnouncementEnabled = configManager.isAnnouncementEnabled();

            // Handle Staff Chat module changes
            if (wasStaffChatEnabled != isStaffChatEnabled) {
                if (isStaffChatEnabled) {
                    initializeStaffChatModule();
                    result.staffChatEnabled = true;
                    result.changes.add("Staff Chat module enabled");
                } else {
                    shutdownStaffChatModule();
                    result.staffChatDisabled = true;
                    result.changes.add("Staff Chat module disabled");
                }
            } else if (isStaffChatEnabled) {
                // Module was already enabled, just reload its configuration
                result.changes.add("Staff Chat configuration reloaded");
            }

            // Handle Private Messages module changes
            if (wasPrivateMessagesEnabled != isPrivateMessagesEnabled) {
                if (isPrivateMessagesEnabled) {
                    initializePrivateMessagesModule();
                    result.privateMessagesEnabled = true;
                    result.changes.add("Private Messages module enabled");
                } else {
                    shutdownPrivateMessagesModule();
                    result.privateMessagesDisabled = true;
                    result.changes.add("Private Messages module disabled");
                }
            } else if (isPrivateMessagesEnabled) {
                // Module was already enabled, just reload its configuration
                result.changes.add("Private Messages configuration reloaded");
            }

            // Handle Lobby Command module changes
            if (wasLobbyCommandEnabled != isLobbyCommandEnabled) {
                if (isLobbyCommandEnabled) {
                    initializeLobbyCommandModule();
                    result.changes.add("Lobby Command module enabled");
                } else {
                    result.changes.add("Lobby Command module disabled");
                }
            } else if (isLobbyCommandEnabled) {
                // Module was already enabled, just reload its configuration
                result.changes.add("Lobby Command configuration reloaded");
            }

            // Handle Announcement module changes
            if (wasAnnouncementEnabled != isAnnouncementEnabled) {
                if (isAnnouncementEnabled) {
                    initializeAnnouncementModule();
                    result.changes.add("Announcement module enabled");
                } else {
                    result.changes.add("Announcement module disabled");
                }
            } else if (isAnnouncementEnabled) {
                // Module was already enabled, just reload its configuration
                result.changes.add("Announcement configuration reloaded");
            }

            result.success = true;

        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
            logger.error("Failed to reload configuration", e);
        }

        return result;
    }

    private void shutdownStaffChatModule() {
        logger.info("Shutting down Staff Chat module...");

        // Note: Velocity doesn't provide a way to unregister commands or listeners
        // So we set the references to null and rely on the module enabled checks
        discordWebhook = null;

        logger.info("Staff Chat module shut down.");
    }

    private void shutdownPrivateMessagesModule() {
        logger.info("Shutting down Private Messages module...");

        // Note: Velocity doesn't provide a way to unregister commands or listeners
        // So we set the references to null and rely on the module enabled checks
        messagingManager = null;

        logger.info("Private Messages module shut down.");
    }

    /**
     * Result class for reload operations
     */
    public static class ReloadResult {
        public boolean success = false;
        public boolean configReloaded = false;
        public boolean staffChatEnabled = false;
        public boolean staffChatDisabled = false;
        public boolean privateMessagesEnabled = false;
        public boolean privateMessagesDisabled = false;
        public String error = null;
        public java.util.List<String> changes = new java.util.ArrayList<>();
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
        if (!isStaffChatModuleEnabled() || discordWebhook == null) {
            return;
        }

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
        if (!isStaffChatModuleEnabled() || discordWebhook == null) {
            return;
        }

        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendConsoleStaffChatMessage(message);
        }
    }

    /**
     * Send a server switch notification for a staff member
     *
     * @param player The player who switched servers
     * @param fromServer The server the player switched from
     * @param toServer The server the player switched to
     */
    public void sendStaffServerSwitchMessage(Player player, String fromServer, String toServer) {
        if (!isStaffChatModuleEnabled() || discordWebhook == null) {
            return;
        }

        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendStaffServerSwitchMessage(player, fromServer, toServer);
        }
    }

    /**
     * Send a connection notification for a staff member
     *
     * @param player The player who connected
     */
    public void sendStaffConnectMessage(Player player) {
        if (!isStaffChatModuleEnabled() || discordWebhook == null) {
            return;
        }

        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendStaffConnectMessage(player);
        }
    }

    /**
     * Send a disconnection notification for a staff member
     *
     * @param player The player who disconnected
     */
    public void sendStaffDisconnectMessage(Player player) {
        if (!isStaffChatModuleEnabled() || discordWebhook == null) {
            return;
        }

        // Send to Discord if enabled
        if (configManager.isDiscordEnabled()) {
            discordWebhook.sendStaffDisconnectMessage(player);
        }
    }
}