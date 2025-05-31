package com.minecraftbangladesh.bmsproxycore.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Path dataDirectory;
    private final Path configFile;
    private final Path staffChatConfigFile;
    private final Path privateMessagesConfigFile;
    private final Path lobbyCommandConfigFile;
    private final Path announcementConfigFile;
    private final Path chatControlConfigFile;

    private Map<String, Object> config;
    private Map<String, Object> staffChatConfig;
    private Map<String, Object> privateMessagesConfig;
    private Map<String, Object> lobbyCommandConfig;
    private Map<String, Object> announcementConfig;
    private Map<String, Object> chatControlConfig;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.yml");
        this.staffChatConfigFile = dataDirectory.resolve("modules").resolve("staffchat.yml");
        this.privateMessagesConfigFile = dataDirectory.resolve("modules").resolve("privatemessages.yml");
        this.lobbyCommandConfigFile = dataDirectory.resolve("modules").resolve("lobbycommand.yml");
        this.announcementConfigFile = dataDirectory.resolve("modules").resolve("announcement.yml");
        this.chatControlConfigFile = dataDirectory.resolve("modules").resolve("chatcontrol.yml");

        this.config = new HashMap<>();
        this.staffChatConfig = new HashMap<>();
        this.privateMessagesConfig = new HashMap<>();
        this.lobbyCommandConfig = new HashMap<>();
        this.announcementConfig = new HashMap<>();
        this.chatControlConfig = new HashMap<>();
    }

    public void loadConfig() {
        try {
            // Create data directory if it doesn't exist
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            // Load main config file
            loadMainConfig();

            // Load module-specific configs based on enabled modules
            if (isStaffChatEnabled()) {
                loadStaffChatConfig();
            }

            if (isPrivateMessagesEnabled()) {
                loadPrivateMessagesConfig();
            }

            if (isLobbyCommandEnabled()) {
                loadLobbyCommandConfig();
            }

            if (isAnnouncementEnabled()) {
                loadAnnouncementConfig();
            }

            if (isChatControlEnabled()) {
                loadChatControlConfig();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMainConfig() throws IOException {
        // Create config file if it doesn't exist
        if (!Files.exists(configFile)) {
            saveDefaultConfig();
        }

        // Load config from file
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            Yaml yaml = new Yaml();
            config = yaml.load(inputStream);
            if (config == null) {
                config = new HashMap<>();
            }
        }
    }

    private void loadStaffChatConfig() throws IOException {
        // Create modules directory if it doesn't exist
        Path modulesDir = dataDirectory.resolve("modules");
        if (!Files.exists(modulesDir)) {
            Files.createDirectories(modulesDir);
        }

        // Create staff chat config file if it doesn't exist
        if (!Files.exists(staffChatConfigFile)) {
            saveDefaultStaffChatConfig();
        }

        // Load staff chat config from file
        try (InputStream inputStream = Files.newInputStream(staffChatConfigFile)) {
            Yaml yaml = new Yaml();
            staffChatConfig = yaml.load(inputStream);
            if (staffChatConfig == null) {
                staffChatConfig = new HashMap<>();
            }
        }
    }

    private void loadPrivateMessagesConfig() throws IOException {
        // Create modules directory if it doesn't exist
        Path modulesDir = dataDirectory.resolve("modules");
        if (!Files.exists(modulesDir)) {
            Files.createDirectories(modulesDir);
        }

        // Create private messages config file if it doesn't exist
        if (!Files.exists(privateMessagesConfigFile)) {
            saveDefaultPrivateMessagesConfig();
        }

        // Load private messages config from file
        try (InputStream inputStream = Files.newInputStream(privateMessagesConfigFile)) {
            Yaml yaml = new Yaml();
            privateMessagesConfig = yaml.load(inputStream);
            if (privateMessagesConfig == null) {
                privateMessagesConfig = new HashMap<>();
            }
        }
    }

    // Module enable/disable methods
    @SuppressWarnings("unchecked")
    public boolean isStaffChatEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Object staffChatObj = modulesSection.get("staffchat");
        if (!(staffChatObj instanceof Map)) return true; // Default to enabled if no staffchat section

        Map<String, Object> staffChatSection = (Map<String, Object>) staffChatObj;
        Object enabled = staffChatSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    @SuppressWarnings("unchecked")
    public boolean isPrivateMessagesEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Object privateMessagesObj = modulesSection.get("private_messages");
        if (!(privateMessagesObj instanceof Map)) return true; // Default to enabled if no private_messages section

        Map<String, Object> privateMessagesSection = (Map<String, Object>) privateMessagesObj;
        Object enabled = privateMessagesSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    @SuppressWarnings("unchecked")
    public boolean isLobbyCommandEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Object lobbyCommandObj = modulesSection.get("lobby_command");
        if (!(lobbyCommandObj instanceof Map)) return true; // Default to enabled if no lobby_command section

        Map<String, Object> lobbyCommandSection = (Map<String, Object>) lobbyCommandObj;
        Object enabled = lobbyCommandSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    @SuppressWarnings("unchecked")
    public boolean isAnnouncementEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Object announcementObj = modulesSection.get("announcement");
        if (!(announcementObj instanceof Map)) return true; // Default to enabled if no announcement section

        Map<String, Object> announcementSection = (Map<String, Object>) announcementObj;
        Object enabled = announcementSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    @SuppressWarnings("unchecked")
    public boolean isChatControlEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Object chatControlObj = modulesSection.get("chatcontrol");
        if (!(chatControlObj instanceof Map)) return true; // Default to enabled if no chatcontrol section

        Map<String, Object> chatControlSection = (Map<String, Object>) chatControlObj;
        Object enabled = chatControlSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    // Save default configuration files
    private void saveDefaultStaffChatConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("modules/staffchat.yml");
             OutputStream outputStream = Files.newOutputStream(staffChatConfigFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultPrivateMessagesConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("modules/privatemessages.yml");
             OutputStream outputStream = Files.newOutputStream(privateMessagesConfigFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLobbyCommandConfig() throws IOException {
        // Create modules directory if it doesn't exist
        Path modulesDir = dataDirectory.resolve("modules");
        if (!Files.exists(modulesDir)) {
            Files.createDirectories(modulesDir);
        }

        // Create lobby command config file if it doesn't exist
        if (!Files.exists(lobbyCommandConfigFile)) {
            saveDefaultLobbyCommandConfig();
        }

        // Load lobby command config from file
        try (InputStream inputStream = Files.newInputStream(lobbyCommandConfigFile)) {
            Yaml yaml = new Yaml();
            lobbyCommandConfig = yaml.load(inputStream);
            if (lobbyCommandConfig == null) {
                lobbyCommandConfig = new HashMap<>();
            }
        }
    }

    private void loadAnnouncementConfig() throws IOException {
        // Create modules directory if it doesn't exist
        Path modulesDir = dataDirectory.resolve("modules");
        if (!Files.exists(modulesDir)) {
            Files.createDirectories(modulesDir);
        }

        // Create announcement config file if it doesn't exist
        if (!Files.exists(announcementConfigFile)) {
            saveDefaultAnnouncementConfig();
        }

        // Load announcement config from file
        try (InputStream inputStream = Files.newInputStream(announcementConfigFile)) {
            Yaml yaml = new Yaml();
            announcementConfig = yaml.load(inputStream);
            if (announcementConfig == null) {
                announcementConfig = new HashMap<>();
            }
        }
    }

    private void loadChatControlConfig() throws IOException {
        // Create modules directory if it doesn't exist
        Path modulesDir = dataDirectory.resolve("modules");
        if (!Files.exists(modulesDir)) {
            Files.createDirectories(modulesDir);
        }

        // Create chat control config file if it doesn't exist
        if (!Files.exists(chatControlConfigFile)) {
            saveDefaultChatControlConfig();
        }

        // Load chat control config from file
        try (InputStream inputStream = Files.newInputStream(chatControlConfigFile)) {
            Yaml yaml = new Yaml();
            chatControlConfig = yaml.load(inputStream);
            if (chatControlConfig == null) {
                chatControlConfig = new HashMap<>();
            }
        }
    }

    private void saveDefaultLobbyCommandConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("modules/lobbycommand.yml");
             OutputStream outputStream = Files.newOutputStream(lobbyCommandConfigFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultAnnouncementConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("modules/announcement.yml");
             OutputStream outputStream = Files.newOutputStream(announcementConfigFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultChatControlConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("modules/chatcontrol.yml");
             OutputStream outputStream = Files.newOutputStream(chatControlConfigFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml");
             OutputStream outputStream = Files.newOutputStream(configFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String getString(String path, String defaultValue) {
        Object value = config.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    public String getString(String path) {
        return getString(path, "");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSection(String path) {
        Object value = config.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    // Helper methods for accessing module-specific configurations
    private String getStaffChatString(String path, String defaultValue) {
        if (!isStaffChatEnabled()) return defaultValue;
        Object value = staffChatConfig.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getStaffChatSection(String path) {
        if (!isStaffChatEnabled()) return new HashMap<>();
        Object value = staffChatConfig.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private String getStaffChatNestedString(String section, String path, String defaultValue) {
        if (!isStaffChatEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getStaffChatSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private String getPrivateMessagesNestedString(String section, String path, String defaultValue) {
        if (!isPrivateMessagesEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getPrivateMessagesSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPrivateMessagesSection(String path) {
        if (!isPrivateMessagesEnabled()) return new HashMap<>();
        Object value = privateMessagesConfig.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private String getLobbyCommandNestedString(String section, String path, String defaultValue) {
        if (!isLobbyCommandEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getLobbyCommandSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLobbyCommandSection(String path) {
        if (!isLobbyCommandEnabled()) return new HashMap<>();
        Object value = lobbyCommandConfig.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private boolean getStaffChatNestedBoolean(String section, String path, boolean defaultValue) {
        if (!isStaffChatEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getStaffChatSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    private int getStaffChatNestedInt(String section, String path, int defaultValue) {
        if (!isStaffChatEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getStaffChatSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getPrivateMessagesString(String path, String defaultValue) {
        if (!isPrivateMessagesEnabled()) return defaultValue;
        Object value = privateMessagesConfig.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    // Helper methods for accessing lobby command configuration
    private String getLobbyCommandString(String path, String defaultValue) {
        if (!isLobbyCommandEnabled()) return defaultValue;
        Object value = lobbyCommandConfig.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private int getLobbyCommandInt(String path, int defaultValue) {
        if (!isLobbyCommandEnabled()) return defaultValue;
        Object value = lobbyCommandConfig.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private java.util.List<String> getLobbyCommandStringList(String path, java.util.List<String> defaultValue) {
        if (!isLobbyCommandEnabled()) return defaultValue;
        Object value = lobbyCommandConfig.get(path);
        return value instanceof java.util.List ? (java.util.List<String>) value : defaultValue;
    }

    public String getNestedString(String section, String path, String defaultValue) {
        Map<String, Object> sectionMap = getSection(section);
        if (sectionMap == null) {
            return defaultValue;
        }
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    public boolean getNestedBoolean(String section, String path, boolean defaultValue) {
        Map<String, Object> sectionMap = getSection(section);
        if (sectionMap == null) {
            return defaultValue;
        }
        Object value = sectionMap.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    public int getNestedInt(String section, String path, int defaultValue) {
        Map<String, Object> sectionMap = getSection(section);
        if (sectionMap == null) {
            return defaultValue;
        }
        Object value = sectionMap.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getStaffChatPrefix() {
        return getStaffChatString("staffchat-prefix", "&b&lStaff &8|");
    }

    public String getMessageFormat() {
        return getStaffChatString("message-format", "{prefix} &7[{server}] &f{player} &8»&r {message}");
    }

    public String getToggleOnMessage() {
        return getStaffChatString("toggle-on-message", "&aStaff chat mode &eenabled&a. Your messages will now go to staff chat.");
    }

    public String getToggleOffMessage() {
        return getStaffChatString("toggle-off-message", "&aStaff chat mode &cdisabled&a. Your messages will now go to public chat.");
    }

    public String getNoPermissionMessage() {
        return getNestedString("global", "no-permission-message", "&cYou don't have permission to use this command.");
    }

    public String getReloadSuccessMessage() {
        return getNestedString("global", "reload-success-message", "&aConfiguration successfully reloaded.");
    }

    public String getReloadFailMessage() {
        return getNestedString("global", "reload-fail-message", "&cFailed to reload configuration. Check console for errors.");
    }

    public String getModuleDisabledMessage() {
        return getNestedString("global", "module-disabled-message", "&cThis feature is currently disabled.");
    }

    public String getModuleConfigMissingMessage() {
        return getNestedString("global", "module-config-missing", "&cConfiguration file for {module} module is missing.");
    }

    // Discord webhook configuration
    public boolean isDiscordEnabled() {
        return getStaffChatNestedBoolean("discord", "enabled", false);
    }

    public String getDiscordWebhookUrl() {
        return getStaffChatNestedString("discord", "webhook-url", "");
    }

    public String getDiscordWebhookName() {
        return getStaffChatNestedString("discord", "webhook-name", "Staff Chat");
    }

    public String getDiscordMessageFormat() {
        return getStaffChatNestedString("discord", "message-format", "**[{server}] {player}**: {message}");
    }

    public boolean usePlayerAvatar() {
        return getStaffChatNestedBoolean("discord", "use-player-avatar", true);
    }

    public int getAvatarSize() {
        return getStaffChatNestedInt("discord", "avatar-size", 128);
    }

    public boolean useAvatarOverlay() {
        return getStaffChatNestedBoolean("discord", "avatar-overlay", true);
    }

    // Staff activity formats
    public String getServerSwitchFormat() {
        return getStaffChatString("server-switch-format", "{prefix} &e{player} &7switched from &6{from_server} &7to &6{to_server}");
    }

    public String getConnectFormat() {
        return getStaffChatString("connect-format", "{prefix} &e{player} &ajoined &7the network");
    }

    public String getDisconnectFormat() {
        return getStaffChatString("disconnect-format", "{prefix} &e{player} &cleft &7the network");
    }

    // Discord staff activity formats
    public String getDiscordServerSwitchFormat() {
        return getStaffChatNestedString("discord", "server-switch-format", "**{player}** switched from **{from_server}** to **{to_server}**");
    }

    public String getDiscordConnectFormat() {
        return getStaffChatNestedString("discord", "connect-format", "**{player}** joined the network");
    }

    public String getDiscordDisconnectFormat() {
        return getStaffChatNestedString("discord", "disconnect-format", "**{player}** left the network");
    }

    // Messaging system configuration
    public String getMessagingSenderFormat() {
        return getPrivateMessagesString("sender-format", "&8[&7You &8→ &7{receiver}&8] &f{message}");
    }

    public String getMessagingReceiverFormat() {
        return getPrivateMessagesString("receiver-format", "&8[&7{sender} &8→ &7You&8] &f{message}");
    }

    public String getMessagingSocialSpyFormat() {
        return getPrivateMessagesString("socialspy-format", "&8[&cSPY&8] &7{sender} &8→ &7{receiver}&8: &f{message}");
    }

    public String getMessagingErrorPlayerNotFound() {
        return getPrivateMessagesString("error-player-not-found", "&cPlayer not found.");
    }

    public String getMessagingErrorMessageSelf() {
        return getPrivateMessagesString("error-message-self", "&cYou cannot message yourself.");
    }

    public String getMessagingErrorNoReplyTarget() {
        return getPrivateMessagesString("error-no-reply-target", "&cYou have nobody to reply to.");
    }

    public String getMessagingErrorInvalidUsageMsg() {
        return getPrivateMessagesString("error-invalid-usage-msg", "&cUsage: /msg <player> <message>");
    }

    public String getMessagingErrorInvalidUsageReply() {
        return getPrivateMessagesString("error-invalid-usage-reply", "&cUsage: /reply <message>");
    }

    public String getMessagingSocialSpyEnabled() {
        return getPrivateMessagesString("socialspy-enabled", "&aSocial spy enabled. You will now see private messages.");
    }

    public String getMessagingSocialSpyDisabled() {
        return getPrivateMessagesString("socialspy-disabled", "&cSocial spy disabled. You will no longer see private messages.");
    }

    public String getMessagingToggleEnabled() {
        return getPrivateMessagesString("msgtoggle-enabled", "&aYou are now accepting private messages.");
    }

    public String getMessagingToggleDisabled() {
        return getPrivateMessagesString("msgtoggle-disabled", "&cYou are no longer accepting private messages.");
    }

    public String getMessagingErrorPlayerToggled() {
        return getPrivateMessagesString("error-player-msgtoggle", "&c{player} is not accepting private messages.");
    }

    public String getMessagingIgnoreAdded() {
        return getPrivateMessagesString("ignore-added", "&aYou are now ignoring {player}.");
    }

    public String getMessagingIgnoreRemoved() {
        return getPrivateMessagesString("ignore-removed", "&aYou are no longer ignoring {player}.");
    }

    public String getMessagingErrorAlreadyIgnoring() {
        return getPrivateMessagesString("error-already-ignoring", "&cYou are already ignoring {player}.");
    }

    public String getMessagingErrorNotIgnoring() {
        return getPrivateMessagesString("error-not-ignoring", "&cYou are not ignoring {player}.");
    }

    public String getMessagingErrorPlayerIgnored() {
        return getPrivateMessagesString("error-player-ignored", "&c{player} is ignoring you.");
    }

    public String getMessagingErrorInvalidUsageIgnore() {
        return getPrivateMessagesString("error-invalid-usage-ignore", "&cUsage: /ignore <add|remove|list> [player]");
    }

    public String getMessagingIgnoreListHeader() {
        return getPrivateMessagesString("ignore-list-header", "&6Players you are ignoring:");
    }

    public String getMessagingIgnoreListEntry() {
        return getPrivateMessagesString("ignore-list-entry", "&7- {player}");
    }

    public String getMessagingIgnoreListEmpty() {
        return getPrivateMessagesString("ignore-list-empty", "&7You are not ignoring any players.");
    }

    // Lobby Command configuration getters
    public String getLobbyTargetServer() {
        return getLobbyCommandString("target-server", "lobby");
    }

    public String getLobbyMainCommand() {
        return getLobbyCommandString("main-command", "lobby");
    }

    public java.util.List<String> getLobbyCommandAliases() {
        return getLobbyCommandStringList("aliases", java.util.Arrays.asList("hub", "spawn", "main", "l"));
    }

    public String getLobbyPermission() {
        return getLobbyUsePermission();
    }

    public String getLobbySuccessMessage() {
        return getLobbyCommandString("success-message", "&aTeleporting to lobby...");
    }

    public String getLobbyAlreadyOnServerMessage() {
        return getLobbyCommandString("already-on-server-message", "&eYou are already on the lobby server!");
    }

    public String getLobbyServerNotFoundMessage() {
        return getLobbyCommandString("server-not-found-message", "&cLobby server is currently unavailable. Please try again later.");
    }

    public String getLobbyTeleportFailedMessage() {
        return getLobbyCommandString("teleport-failed-message", "&cFailed to teleport to lobby. Please try again.");
    }

    public String getLobbyNoPermissionMessage() {
        return getLobbyCommandString("no-permission-message", "&cYou don't have permission to use this command.");
    }

    public int getLobbyCooldown() {
        return getLobbyCommandInt("cooldown", 3);
    }

    public String getLobbyCooldownMessage() {
        return getLobbyCommandString("cooldown-message", "&cYou must wait {time} seconds before using this command again.");
    }

    public String getLobbyCooldownBypassPermission() {
        return getLobbyCommandNestedString("permissions", "bypass-cooldown", "bmsproxycore.lobby.cooldown.bypass");
    }

    // Staff Chat Permission getters
    public String getStaffChatUsePermission() {
        return getStaffChatNestedString("permissions", "use", "bmsproxycore.staffchat.use");
    }

    public String getStaffChatTogglePermission() {
        return getStaffChatNestedString("permissions", "toggle", "bmsproxycore.staffchat.toggle");
    }

    public String getStaffChatReloadPermission() {
        return getStaffChatNestedString("permissions", "reload", "bmsproxycore.staffchat.reload");
    }

    public String getStaffChatActivityPermission() {
        return getStaffChatNestedString("permissions", "activity", "bmsproxycore.staffchat.activity");
    }

    // Private Messages Permission getters
    public String getPrivateMessagesSendPermission() {
        return getPrivateMessagesNestedString("permissions", "send", "bmsproxycore.message.send");
    }

    public String getPrivateMessagesReplyPermission() {
        return getPrivateMessagesNestedString("permissions", "reply", "bmsproxycore.message.reply");
    }

    public String getPrivateMessagesSocialSpyPermission() {
        return getPrivateMessagesNestedString("permissions", "socialspy", "bmsproxycore.message.socialspy");
    }

    public String getPrivateMessagesTogglePermission() {
        return getPrivateMessagesNestedString("permissions", "toggle", "bmsproxycore.message.toggle");
    }

    public String getPrivateMessagesIgnorePermission() {
        return getPrivateMessagesNestedString("permissions", "ignore", "bmsproxycore.message.ignore");
    }

    public String getPrivateMessagesBypassTogglePermission() {
        return getPrivateMessagesNestedString("permissions", "bypass-toggle", "bmsproxycore.message.bypass.toggle");
    }

    public String getPrivateMessagesBypassIgnorePermission() {
        return getPrivateMessagesNestedString("permissions", "bypass-ignore", "bmsproxycore.message.bypass.ignore");
    }

    // Lobby Command Permission getters
    public String getLobbyUsePermission() {
        return getLobbyCommandNestedString("permissions", "use", "bmsproxycore.lobby.use");
    }

    // Announcement configuration helper methods
    private String getAnnouncementString(String path, String defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Object value = announcementConfig.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }



    @SuppressWarnings("unchecked")
    private Map<String, Object> getAnnouncementSection(String path) {
        if (!isAnnouncementEnabled()) return new HashMap<>();
        Object value = announcementConfig.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private String getAnnouncementNestedString(String section, String path, String defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getAnnouncementSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private boolean getAnnouncementNestedBoolean(String section, String path, boolean defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getAnnouncementSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }



    @SuppressWarnings("unchecked")
    private java.util.List<String> getAnnouncementStringList(String path, java.util.List<String> defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Object value = announcementConfig.get(path);
        return value instanceof java.util.List ? (java.util.List<String>) value : defaultValue;
    }

    // Announcement configuration getters
    public String getAnnouncementMainCommand() {
        return getAnnouncementString("main-command", "announce");
    }

    public java.util.List<String> getAnnouncementCommandAliases() {
        return getAnnouncementStringList("aliases", java.util.Arrays.asList("announcement", "alert"));
    }

    public String getAnnouncementSendPermission() {
        return getAnnouncementNestedString("permissions", "send", "bmsproxycore.announcement.send");
    }

    public boolean isAnnouncementTitleEnabled() {
        return getAnnouncementNestedBoolean("title", "enabled", true);
    }

    public String getAnnouncementTitleMainTitle() {
        return getAnnouncementNestedString("title", "main-title", "&c&l< ALERT >");
    }

    public String getAnnouncementTitleSubtitle() {
        return getAnnouncementNestedString("title", "subtitle", "&f{announcement}");
    }

    @SuppressWarnings("unchecked")
    public int getAnnouncementTitleFadeIn() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Object timingObj = titleSection.get("timing");
        if (!(timingObj instanceof Map)) return 10;

        Map<String, Object> timingSection = (Map<String, Object>) timingObj;
        Object value = timingSection.get("fade-in");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 10;
            }
        }
        return 10;
    }

    @SuppressWarnings("unchecked")
    public int getAnnouncementTitleStay() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Object timingObj = titleSection.get("timing");
        if (!(timingObj instanceof Map)) return 60;

        Map<String, Object> timingSection = (Map<String, Object>) timingObj;
        Object value = timingSection.get("stay");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 60;
            }
        }
        return 60;
    }

    @SuppressWarnings("unchecked")
    public int getAnnouncementTitleFadeOut() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Object timingObj = titleSection.get("timing");
        if (!(timingObj instanceof Map)) return 10;

        Map<String, Object> timingSection = (Map<String, Object>) timingObj;
        Object value = timingSection.get("fade-out");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 10;
            }
        }
        return 10;
    }

    public boolean isAnnouncementChatMessageEnabled() {
        return getAnnouncementNestedBoolean("chat-message", "enabled", true);
    }

    public String getAnnouncementChatMessageFormat() {
        return getAnnouncementNestedString("chat-message", "format", "&8[&c&lALERT&8] &f{announcement}");
    }

    public String getAnnouncementConsoleFormat() {
        return getAnnouncementNestedString("chat-message", "console-format", "&8[&c&lALERT&8] &7[Console] &f{announcement}");
    }

    public boolean isAnnouncementNetworkEnabled() {
        return getAnnouncementNestedBoolean("network", "enabled", true);
    }

    public boolean isAnnouncementShowSender() {
        return getAnnouncementNestedBoolean("network", "show-sender", false);
    }

    public String getAnnouncementSenderFormat() {
        return getAnnouncementNestedString("network", "sender-format", "&8[&c&lALERT&8] &7[{server}] &e{sender}&8: &f{announcement}");
    }

    public String getAnnouncementSuccessMessage() {
        return getAnnouncementString("success-message", "&aAnnouncement sent to all players across the network!");
    }

    public String getAnnouncementEmptyMessage() {
        return getAnnouncementString("empty-message", "&cPlease provide an announcement message.");
    }

    public String getAnnouncementNoPermissionMessage() {
        return getAnnouncementString("no-permission-message", "&cYou don't have permission to send announcements.");
    }

    public String getAnnouncementUsageMessage() {
        return getAnnouncementString("usage-message", "&cUsage: /{command} <message>");
    }

    public String getAnnouncementConsoleUsageMessage() {
        return getAnnouncementString("console-usage-message", "&7Usage: /{command} <message> - Send an announcement to all players");
    }

    // Chat Control Configuration Methods
    // Helper methods for accessing chat control configuration

    @SuppressWarnings("unchecked")
    private Map<String, Object> getChatControlSection(String path) {
        if (!isChatControlEnabled()) return new HashMap<>();
        Object value = chatControlConfig.get(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private String getChatControlNestedString(String section, String path, String defaultValue) {
        if (!isChatControlEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getChatControlSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private String getChatControlDoubleNestedString(String section, String subsection, String path, String defaultValue) {
        if (!isChatControlEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getChatControlSection(section);
        if (sectionMap == null) return defaultValue;

        Object subsectionObj = sectionMap.get(subsection);
        if (!(subsectionObj instanceof Map)) return defaultValue;

        @SuppressWarnings("unchecked")
        Map<String, Object> subsectionMap = (Map<String, Object>) subsectionObj;
        Object value = subsectionMap.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private int getChatControlNestedInt(String section, String path, int defaultValue) {
        if (!isChatControlEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getChatControlSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof Integer ? (Integer) value : defaultValue;
    }

    private boolean getChatControlNestedBoolean(String section, String path, boolean defaultValue) {
        if (!isChatControlEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getChatControlSection(section);
        if (sectionMap == null) return defaultValue;
        Object value = sectionMap.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    // Component enable/disable methods
    public boolean isChatFilterEnabled() {
        return getChatControlNestedBoolean("components", "filter", true) &&
               getChatControlNestedBoolean("components", "enabled", true);
    }

    public boolean isChatCooldownEnabled() {
        return getChatControlNestedBoolean("components", "cooldown", true) &&
               getChatControlNestedBoolean("components", "enabled", true);
    }

    // Permission methods
    public String getChatFilterManagePermission() {
        return getChatControlDoubleNestedString("permissions", "filter", "manage", "bmsproxycore.chatcontrol.filter.manage");
    }

    public String getChatFilterBypassPermission() {
        return getChatControlDoubleNestedString("permissions", "filter", "bypass", "bmsproxycore.chatcontrol.filter.bypass");
    }

    public String getChatFilterReloadPermission() {
        return getChatControlDoubleNestedString("permissions", "filter", "reload", "bmsproxycore.chatcontrol.filter.reload");
    }

    public String getChatCooldownManagePermission() {
        return getChatControlDoubleNestedString("permissions", "cooldown", "manage", "bmsproxycore.chatcontrol.cooldown.manage");
    }

    public String getChatCooldownBypassPermission() {
        return getChatControlDoubleNestedString("permissions", "cooldown", "bypass", "bmsproxycore.chatcontrol.cooldown.bypass");
    }

    public String getChatCooldownReloadPermission() {
        return getChatControlDoubleNestedString("permissions", "cooldown", "reload", "bmsproxycore.chatcontrol.cooldown.reload");
    }

    // Chat Filter Configuration
    @SuppressWarnings("unchecked")
    public java.util.List<String> getChatFilterRules() {
        Map<String, Object> filterSection = getChatControlSection("filter");
        Object rulesObj = filterSection.get("rules");
        if (rulesObj instanceof java.util.List) {
            return (java.util.List<String>) rulesObj;
        }
        return new java.util.ArrayList<>();
    }

    public String getChatFilterAction() {
        return getChatControlNestedString("filter", "action", "warn");
    }

    public String getChatFilterBlockedMessage() {
        return getChatControlNestedString("filter", "blocked-message", "&cYour message was blocked by the chat filter.");
    }

    public String getChatFilterReplacementText() {
        return getChatControlNestedString("filter", "replacement-text", "***");
    }

    public boolean isChatFilterLogEnabled() {
        return getChatControlNestedBoolean("filter", "log-filtered", true);
    }

    public String getChatFilterLogFormat() {
        return getChatControlNestedString("filter", "log-format", "[ChatFilter] {player} attempted to send: {message}");
    }

    public boolean isChatFilterCaseSensitive() {
        return getChatControlNestedBoolean("advanced", "case-sensitive", false);
    }

    // Chat Cooldown Configuration
    public int getChatCooldownDuration() {
        return getChatControlNestedInt("cooldown", "duration", 3);
    }

    public String getChatCooldownMessage() {
        return getChatControlNestedString("cooldown", "cooldown-message", "&cYou must wait {time} seconds before sending another message.");
    }

    public boolean isChatCooldownPermissionBasedEnabled() {
        return getChatControlNestedBoolean("cooldown", "permission-based", "enabled", false);
    }

    @SuppressWarnings("unchecked")
    public java.util.Map<String, Integer> getChatCooldownPermissionDurations() {
        if (!isChatControlEnabled()) return new java.util.HashMap<>();

        Map<String, Object> cooldownSection = getChatControlSection("cooldown");
        Object permissionBasedObj = cooldownSection.get("permission-based");
        if (!(permissionBasedObj instanceof Map)) return new java.util.HashMap<>();

        Map<String, Object> permissionBasedSection = (Map<String, Object>) permissionBasedObj;
        Object durationsObj = permissionBasedSection.get("durations");
        if (!(durationsObj instanceof Map)) return new java.util.HashMap<>();

        Map<String, Object> durationsSection = (Map<String, Object>) durationsObj;
        java.util.Map<String, Integer> result = new java.util.HashMap<>();

        for (Map.Entry<String, Object> entry : durationsSection.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                result.put(entry.getKey(), (Integer) entry.getValue());
            }
        }

        return result;
    }

    public boolean isChatCooldownLogViolationsEnabled() {
        return getChatControlNestedBoolean("cooldown", "log-violations", false);
    }

    public String getChatCooldownViolationLogFormat() {
        return getChatControlNestedString("cooldown", "violation-log-format", "[ChatCooldown] {player} tried to send message too quickly");
    }

    // Messages Configuration
    public String getChatControlNoPermissionMessage() {
        return getChatControlNestedString("messages", "no-permission", "&cYou don't have permission to use this command.");
    }

    public String getChatControlModuleDisabledMessage() {
        return getChatControlNestedString("messages", "module-disabled", "&cThis feature is currently disabled.");
    }

    public String getChatControlReloadSuccessMessage() {
        return getChatControlNestedString("messages", "reload-success", "&aChat control configuration reloaded successfully.");
    }

    public String getChatControlReloadFailedMessage() {
        return getChatControlNestedString("messages", "reload-failed", "&cFailed to reload chat control configuration.");
    }

    // Filter Messages
    public String getChatFilterRuleAddedMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "rule-added", "&aFilter rule added: &7{rule}");
    }

    public String getChatFilterRuleRemovedMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "rule-removed", "&aFilter rule removed: &7{rule}");
    }

    public String getChatFilterRuleNotFoundMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "rule-not-found", "&cFilter rule not found: &7{rule}");
    }

    public String getChatFilterRuleAlreadyExistsMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "rule-already-exists", "&cFilter rule already exists: &7{rule}");
    }

    public String getChatFilterInvalidRegexMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "invalid-regex", "&cInvalid regex pattern: &7{rule}");
    }

    public String getChatFilterListHeaderMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "list-header", "&6Active Filter Rules:");
    }

    public String getChatFilterListFormatMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "list-format", "&7- {rule}");
    }

    public String getChatFilterListEmptyMessage() {
        return getChatControlDoubleNestedString("messages", "filter", "list-empty", "&7No filter rules are currently active.");
    }

    // Cooldown Messages
    public String getChatCooldownDurationSetMessage() {
        return getChatControlDoubleNestedString("messages", "cooldown", "duration-set", "&aCooldown duration set to &7{duration} &aseconds.");
    }

    public String getChatCooldownDurationDisabledMessage() {
        return getChatControlDoubleNestedString("messages", "cooldown", "duration-disabled", "&aCooldown has been disabled.");
    }

    public String getChatCooldownInvalidDurationMessage() {
        return getChatControlDoubleNestedString("messages", "cooldown", "invalid-duration", "&cInvalid duration. Please enter a number.");
    }

    public String getChatCooldownStatusEnabledMessage() {
        return getChatControlDoubleNestedString("messages", "cooldown", "status-enabled", "&aCooldown is currently &aenabled &7({duration}s)");
    }

    public String getChatCooldownStatusDisabledMessage() {
        return getChatControlDoubleNestedString("messages", "cooldown", "status-disabled", "&aCooldown is currently &cdisabled");
    }

    // Advanced Configuration
    public int getChatControlMaxFilterRules() {
        return getChatControlNestedInt("advanced", "max-filter-rules", 50);
    }

    public boolean isChatControlDebugEnabled() {
        return getChatControlNestedBoolean("advanced", "debug", false);
    }

    public String getChatControlDebugFormat() {
        return getChatControlNestedString("advanced", "debug-format", "[ChatControl-Debug] {component}: {message}");
    }

    // Command Configuration
    public String getChatFilterMainCommand() {
        return getChatControlDoubleNestedString("commands", "filter", "main-command", "chatfilter");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getChatFilterCommandAliases() {
        Map<String, Object> commandsSection = getChatControlSection("commands");
        Object filterObj = commandsSection.get("filter");
        if (!(filterObj instanceof Map)) return java.util.Arrays.asList("cf", "filter");

        Map<String, Object> filterSection = (Map<String, Object>) filterObj;
        Object aliasesObj = filterSection.get("aliases");
        if (aliasesObj instanceof java.util.List) {
            return (java.util.List<String>) aliasesObj;
        }
        return java.util.Arrays.asList("cf", "filter");
    }

    public String getChatCooldownMainCommand() {
        return getChatControlDoubleNestedString("commands", "cooldown", "main-command", "chatcooldown");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getChatCooldownCommandAliases() {
        Map<String, Object> commandsSection = getChatControlSection("commands");
        Object cooldownObj = commandsSection.get("cooldown");
        if (!(cooldownObj instanceof Map)) return java.util.Arrays.asList("cc", "cooldown");

        Map<String, Object> cooldownSection = (Map<String, Object>) cooldownObj;
        Object aliasesObj = cooldownSection.get("aliases");
        if (aliasesObj instanceof java.util.List) {
            return (java.util.List<String>) aliasesObj;
        }
        return java.util.Arrays.asList("cc", "cooldown");
    }

    // Methods for dynamic configuration updates (used by commands)
    public void addChatFilterRule(String rule) {
        // This would need to be implemented to save to config file
        // For now, it's a placeholder for the command functionality
    }

    public void removeChatFilterRule(String rule) {
        // This would need to be implemented to save to config file
        // For now, it's a placeholder for the command functionality
    }

    public void setChatCooldownDuration(int duration) {
        // This would need to be implemented to save to config file
        // For now, it's a placeholder for the command functionality
    }

    // Helper method for cooldown component to check if there's a double nested boolean
    private boolean getChatControlNestedBoolean(String section, String subsection, String path, boolean defaultValue) {
        if (!isChatControlEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getChatControlSection(section);
        if (sectionMap == null) return defaultValue;

        Object subsectionObj = sectionMap.get(subsection);
        if (!(subsectionObj instanceof Map)) return defaultValue;

        @SuppressWarnings("unchecked")
        Map<String, Object> subsectionMap = (Map<String, Object>) subsectionObj;
        Object value = subsectionMap.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}