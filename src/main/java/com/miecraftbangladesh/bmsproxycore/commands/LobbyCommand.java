package com.miecraftbangladesh.bmsproxycore.commands;

import com.miecraftbangladesh.bmsproxycore.BMSProxyCore;
import com.miecraftbangladesh.bmsproxycore.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyCommand implements SimpleCommand {

    private final BMSProxyCore plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public LobbyCommand(BMSProxyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        // Check if Lobby Command module is enabled
        if (!plugin.isLobbyCommandModuleEnabled()) {
            source.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getModuleDisabledMessage()));
            return;
        }

        if (!(source instanceof Player)) {
            source.sendMessage(MessageUtils.formatMessage("&cThis command can only be executed by a player."));
            return;
        }

        Player player = (Player) source;

        // Check permission
        String permission = plugin.getConfigManager().getLobbyUsePermission();
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getLobbyNoPermissionMessage()));
            return;
        }

        // Check cooldown
        if (!checkCooldown(player)) {
            return;
        }

        // Get target server
        String targetServerName = plugin.getConfigManager().getLobbyTargetServer();
        RegisteredServer targetServer = plugin.getServer().getServer(targetServerName).orElse(null);

        if (targetServer == null) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getLobbyServerNotFoundMessage()));
            return;
        }

        // Check if player is already on the target server
        if (player.getCurrentServer().isPresent() &&
            player.getCurrentServer().get().getServerInfo().getName().equals(targetServerName)) {
            player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getLobbyAlreadyOnServerMessage()));
            return;
        }

        // Send success message
        player.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getLobbySuccessMessage()));

        // Connect to server
        player.createConnectionRequest(targetServer).fireAndForget();

        // Set cooldown
        setCooldown(player);
    }

    private boolean checkCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        int cooldownSeconds = plugin.getConfigManager().getLobbyCooldown();

        // No cooldown if set to 0 or less
        if (cooldownSeconds <= 0) {
            return true;
        }

        // Check bypass permission
        String bypassPermission = plugin.getConfigManager().getLobbyCooldownBypassPermission();
        if (!bypassPermission.isEmpty() && player.hasPermission(bypassPermission)) {
            return true;
        }

        Long lastUsed = cooldowns.get(playerId);
        if (lastUsed == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUsed;
        long cooldownMillis = cooldownSeconds * 1000L;

        if (timeDiff < cooldownMillis) {
            long remainingSeconds = (cooldownMillis - timeDiff) / 1000L + 1;
            String message = plugin.getConfigManager().getLobbyCooldownMessage()
                    .replace("{time}", String.valueOf(remainingSeconds));
            player.sendMessage(MessageUtils.formatMessage(message));
            return false;
        }

        return true;
    }

    private void setCooldown(Player player) {
        int cooldownSeconds = plugin.getConfigManager().getLobbyCooldown();
        if (cooldownSeconds > 0) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            return false;
        }

        String permission = plugin.getConfigManager().getLobbyUsePermission();
        if (permission.isEmpty()) {
            return true; // No permission required
        }

        return invocation.source().hasPermission(permission);
    }
}
