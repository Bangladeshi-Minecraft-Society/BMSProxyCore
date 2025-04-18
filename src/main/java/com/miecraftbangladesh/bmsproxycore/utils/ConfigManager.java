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
    private Map<String, Object> config;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.yml");
        this.config = new HashMap<>();
    }

    public void loadConfig() {
        try {
            // Create data directory if it doesn't exist
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

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
            
            // Check for new configuration options and add them if missing
            boolean configUpdated = false;
            
            // Check in-game formatting options
            if (!config.containsKey("server-switch-format")) {
                config.put("server-switch-format", "{prefix} &e{player} &7switched from &6{from_server} &7to &6{to_server}");
                configUpdated = true;
            }
            
            if (!config.containsKey("connect-format")) {
                config.put("connect-format", "{prefix} &e{player} &ajoined &7the network");
                configUpdated = true;
            }
            
            if (!config.containsKey("disconnect-format")) {
                config.put("disconnect-format", "{prefix} &e{player} &cleft &7the network");
                configUpdated = true;
            }
            
            // Check Discord webhook formatting options
            Map<String, Object> discordSection = getSection("discord");
            if (!discordSection.containsKey("server-switch-format")) {
                discordSection.put("server-switch-format", "**{player}** switched from **{from_server}** to **{to_server}**");
                config.put("discord", discordSection);
                configUpdated = true;
            }
            
            if (!discordSection.containsKey("connect-format")) {
                discordSection.put("connect-format", "**{player}** joined the network");
                config.put("discord", discordSection);
                configUpdated = true;
            }
            
            if (!discordSection.containsKey("disconnect-format")) {
                discordSection.put("disconnect-format", "**{player}** left the network");
                config.put("discord", discordSection);
                configUpdated = true;
            }
            
            // Save the config if it was updated
            if (configUpdated) {
                saveConfig();
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
        return getString("staffchat-prefix", "&b&lStaff &8|");
    }

    public String getMessageFormat() {
        return getString("message-format", "{prefix} &7[{server}] &f{player} &8»&r {message}");
    }

    public String getToggleOnMessage() {
        return getString("toggle-on-message", "&aStaff chat mode &eenabled&a. Your messages will now go to staff chat.");
    }

    public String getToggleOffMessage() {
        return getString("toggle-off-message", "&aStaff chat mode &cdisabled&a. Your messages will now go to public chat.");
    }

    public String getNoPermissionMessage() {
        return getString("no-permission-message", "&cYou don't have permission to use this command.");
    }

    public String getReloadSuccessMessage() {
        return getString("reload-success-message", "&aConfiguration successfully reloaded.");
    }

    public String getReloadFailMessage() {
        return getString("reload-fail-message", "&cFailed to reload configuration. Check console for errors.");
    }

    // Discord webhook configuration
    public boolean isDiscordEnabled() {
        return getNestedBoolean("discord", "enabled", false);
    }

    public String getDiscordWebhookUrl() {
        return getNestedString("discord", "webhook-url", "");
    }

    public String getDiscordWebhookName() {
        return getNestedString("discord", "webhook-name", "Staff Chat");
    }

    public String getDiscordMessageFormat() {
        return getNestedString("discord", "message-format", "**[{server}] {player}**: {message}");
    }

    public boolean usePlayerAvatar() {
        return getNestedBoolean("discord", "use-player-avatar", true);
    }

    public int getAvatarSize() {
        return getNestedInt("discord", "avatar-size", 128);
    }

    public boolean useAvatarOverlay() {
        return getNestedBoolean("discord", "avatar-overlay", true);
    }
    
    // Staff activity formats
    public String getServerSwitchFormat() {
        return getString("server-switch-format", "{prefix} &e{player} &7switched from &6{from_server} &7to &6{to_server}");
    }
    
    public String getConnectFormat() {
        return getString("connect-format", "{prefix} &e{player} &ajoined &7the network");
    }
    
    public String getDisconnectFormat() {
        return getString("disconnect-format", "{prefix} &e{player} &cleft &7the network");
    }
    
    // Discord staff activity formats
    public String getDiscordServerSwitchFormat() {
        return getNestedString("discord", "server-switch-format", "**{player}** switched from **{from_server}** to **{to_server}**");
    }
    
    public String getDiscordConnectFormat() {
        return getNestedString("discord", "connect-format", "**{player}** joined the network");
    }
    
    public String getDiscordDisconnectFormat() {
        return getNestedString("discord", "disconnect-format", "**{player}** left the network");
    }
    
    // Messaging system configuration
    public String getMessagingSenderFormat() {
        return getNestedString("messaging", "sender-format", "&8[&7You &8→ &7{receiver}&8] &f{message}");
    }
    
    public String getMessagingReceiverFormat() {
        return getNestedString("messaging", "receiver-format", "&8[&7{sender} &8→ &7You&8] &f{message}");
    }
    
    public String getMessagingSocialSpyFormat() {
        return getNestedString("messaging", "socialspy-format", "&8[&cSPY&8] &7{sender} &8→ &7{receiver}&8: &f{message}");
    }
    
    public String getMessagingErrorPlayerNotFound() {
        return getNestedString("messaging", "error-player-not-found", "&cPlayer not found.");
    }
    
    public String getMessagingErrorMessageSelf() {
        return getNestedString("messaging", "error-message-self", "&cYou cannot message yourself.");
    }
    
    public String getMessagingErrorNoReplyTarget() {
        return getNestedString("messaging", "error-no-reply-target", "&cYou have nobody to reply to.");
    }
    
    public String getMessagingErrorInvalidUsageMsg() {
        return getNestedString("messaging", "error-invalid-usage-msg", "&cUsage: /msg <player> <message>");
    }
    
    public String getMessagingErrorInvalidUsageReply() {
        return getNestedString("messaging", "error-invalid-usage-reply", "&cUsage: /reply <message>");
    }
    
    public String getMessagingSocialSpyEnabled() {
        return getNestedString("messaging", "socialspy-enabled", "&aSocial spy enabled. You will now see private messages.");
    }
    
    public String getMessagingSocialSpyDisabled() {
        return getNestedString("messaging", "socialspy-disabled", "&cSocial spy disabled. You will no longer see private messages.");
    }
    
    public String getMessagingToggleEnabled() {
        return getNestedString("messaging", "msgtoggle-enabled", "&aYou are now accepting private messages.");
    }
    
    public String getMessagingToggleDisabled() {
        return getNestedString("messaging", "msgtoggle-disabled", "&cYou are no longer accepting private messages.");
    }
    
    public String getMessagingErrorPlayerToggled() {
        return getNestedString("messaging", "error-player-msgtoggle", "&c{player} is not accepting private messages.");
    }
    
    public String getMessagingIgnoreAdded() {
        return getNestedString("messaging", "ignore-added", "&aYou are now ignoring {player}.");
    }
    
    public String getMessagingIgnoreRemoved() {
        return getNestedString("messaging", "ignore-removed", "&aYou are no longer ignoring {player}.");
    }
    
    public String getMessagingErrorAlreadyIgnoring() {
        return getNestedString("messaging", "error-already-ignoring", "&cYou are already ignoring {player}.");
    }
    
    public String getMessagingErrorNotIgnoring() {
        return getNestedString("messaging", "error-not-ignoring", "&cYou are not ignoring {player}.");
    }
    
    public String getMessagingErrorPlayerIgnored() {
        return getNestedString("messaging", "error-player-ignored", "&c{player} is ignoring you.");
    }
    
    public String getMessagingErrorInvalidUsageIgnore() {
        return getNestedString("messaging", "error-invalid-usage-ignore", "&cUsage: /ignore <add|remove|list> [player]");
    }
    
    public String getMessagingIgnoreListHeader() {
        return getNestedString("messaging", "ignore-list-header", "&6Players you are ignoring:");
    }
    
    public String getMessagingIgnoreListEntry() {
        return getNestedString("messaging", "ignore-list-entry", "&7- {player}");
    }
    
    public String getMessagingIgnoreListEmpty() {
        return getNestedString("messaging", "ignore-list-empty", "&7You are not ignoring any players.");
    }
} 