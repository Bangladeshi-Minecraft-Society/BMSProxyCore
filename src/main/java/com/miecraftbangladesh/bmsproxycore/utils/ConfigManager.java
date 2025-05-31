package com.miecraftbangladesh.bmsproxycore.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileWriter;
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

    private Map<String, Object> config;
    private Map<String, Object> staffChatConfig;
    private Map<String, Object> privateMessagesConfig;
    private Map<String, Object> lobbyCommandConfig;
    private Map<String, Object> announcementConfig;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.yml");
        this.staffChatConfigFile = dataDirectory.resolve("staffchat.yml");
        this.privateMessagesConfigFile = dataDirectory.resolve("privatemessages.yml");
        this.lobbyCommandConfigFile = dataDirectory.resolve("lobbycommand.yml");
        this.announcementConfigFile = dataDirectory.resolve("announcement.yml");

        this.config = new HashMap<>();
        this.staffChatConfig = new HashMap<>();
        this.privateMessagesConfig = new HashMap<>();
        this.lobbyCommandConfig = new HashMap<>();
        this.announcementConfig = new HashMap<>();
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
    public boolean isStaffChatEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Map<String, Object> staffChatSection = (Map<String, Object>) modulesSection.get("staffchat");
        if (staffChatSection == null) return true; // Default to enabled if no staffchat section

        Object enabled = staffChatSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    public boolean isPrivateMessagesEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Map<String, Object> privateMessagesSection = (Map<String, Object>) modulesSection.get("private_messages");
        if (privateMessagesSection == null) return true; // Default to enabled if no private_messages section

        Object enabled = privateMessagesSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    public boolean isLobbyCommandEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Map<String, Object> lobbyCommandSection = (Map<String, Object>) modulesSection.get("lobby_command");
        if (lobbyCommandSection == null) return true; // Default to enabled if no lobby_command section

        Object enabled = lobbyCommandSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    public boolean isAnnouncementEnabled() {
        Map<String, Object> modulesSection = getSection("modules");
        if (modulesSection == null) return true; // Default to enabled if no modules section

        Map<String, Object> announcementSection = (Map<String, Object>) modulesSection.get("announcement");
        if (announcementSection == null) return true; // Default to enabled if no announcement section

        Object enabled = announcementSection.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : true;
    }

    // Save default configuration files
    private void saveDefaultStaffChatConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("staffchat.yml");
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
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("privatemessages.yml");
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

    private void saveDefaultLobbyCommandConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("lobbycommand.yml");
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
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("announcement.yml");
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

    private void saveConfig() {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                yaml.dump(config, writer);
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

    private boolean getAnnouncementBoolean(String path, boolean defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Object value = announcementConfig.get(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    private int getAnnouncementInt(String path, int defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Object value = announcementConfig.get(path);
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

    private int getAnnouncementNestedInt(String section, String path, int defaultValue) {
        if (!isAnnouncementEnabled()) return defaultValue;
        Map<String, Object> sectionMap = getAnnouncementSection(section);
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

    public int getAnnouncementTitleFadeIn() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Map<String, Object> timingSection = (Map<String, Object>) titleSection.get("timing");
        if (timingSection == null) return 10;
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

    public int getAnnouncementTitleStay() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Map<String, Object> timingSection = (Map<String, Object>) titleSection.get("timing");
        if (timingSection == null) return 60;
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

    public int getAnnouncementTitleFadeOut() {
        Map<String, Object> titleSection = getAnnouncementSection("title");
        Map<String, Object> timingSection = (Map<String, Object>) titleSection.get("timing");
        if (timingSection == null) return 10;
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
}