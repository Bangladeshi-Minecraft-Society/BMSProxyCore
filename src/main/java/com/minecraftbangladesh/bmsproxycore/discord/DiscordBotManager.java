package com.minecraftbangladesh.bmsproxycore.discord;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discord bot manager to listen to a staff channel and forward messages to in-game staff chat.
 */
public class DiscordBotManager extends ListenerAdapter {

    private final BMSProxyCore plugin;
    private JDA jda;
    private final Set<String> staffChannelIds = new HashSet<>();

    public DiscordBotManager(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize and start the Discord bot if enabled and properly configured.
     */
    public void initialize() {
        if (!plugin.getConfigManager().isDiscordBotEnabled()) {
            plugin.getLogger().info("Discord bot integration is disabled in configuration");
            return;
        }

        String token = plugin.getConfigManager().getDiscordBotToken();
        if (token == null || token.isBlank() || token.equalsIgnoreCase("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().warn("Discord bot token is not configured. Skipping Discord bot initialization.");
            return;
        }

        List<String> channels = plugin.getConfigManager().getDiscordBotStaffChannelIds();
        if (channels == null || channels.isEmpty()) {
            plugin.getLogger().warn("No Discord staff channel IDs configured. Skipping Discord bot initialization.");
            return;
        }
        staffChannelIds.clear();
        staffChannelIds.addAll(channels);

        try {
            JDABuilder builder = JDABuilder.createDefault(token,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT
            );

            builder.addEventListeners(this);

            jda = builder.build();

            plugin.getLogger().info("Discord bot initialized. Listening to " + staffChannelIds.size() + " staff channel(s).");
        } catch (Exception e) {
            plugin.getLogger().error("Failed to initialize Discord bot", e);
        }
    }

    /**
     * Cleanly shut down the Discord bot.
     */
    public void shutdown() {
        if (jda != null) {
            try {
                jda.shutdownNow();
            } catch (Exception ignored) {
            }
            jda = null;
            plugin.getLogger().info("Discord bot shut down");
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Ignore DMs and group DMs; only process guild text channels
        if (!event.isFromGuild()) {
            return;
        }

        // Ignore messages from bots and webhooks to prevent loops/echo
        if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage()) {
            return;
        }

        MessageChannel channel = event.getChannel();

        // Only process configured staff channels
        if (!staffChannelIds.contains(channel.getId())) {
            return;
        }

        String discordUsername = event.getAuthor().getName(); // Username, not display/nickname
        String content = event.getMessage().getContentDisplay();

        if (content == null || content.isBlank()) {
            return; // Ignore empty content (attachments/embeds-only) for now
        }

        // Forward to in-game staff chat on this proxy, and broadcast cross-proxy if enabled
        plugin.handleDiscordInboundMessage(discordUsername, content);
    }
}
