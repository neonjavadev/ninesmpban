package com.ninesmp.bansystem.listeners;

import com.ninesmp.bansystem.BanSystemPlugin;
import com.ninesmp.bansystem.api.ApiClient;
import com.ninesmp.bansystem.models.Ban;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerJoinListener implements Listener {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public PlayerJoinListener(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String uuid = event.getUniqueId().toString();
        String ip = event.getAddress().getHostAddress();

        // Check for UUID ban
        Ban uuidBan = apiClient.getActiveBanByUuid(uuid);
        if (uuidBan != null) {
            String kickMessage = formatBanMessage(uuidBan);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
            return;
        }

        // Check for IP ban
        Ban ipBan = apiClient.getActiveBanByIp(ip);
        if (ipBan != null) {
            String kickMessage = formatBanMessage(ipBan);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
        }
    }

    /**
     * Format ban message for display
     */
    private String formatBanMessage(Ban ban) {
        String template;
        if (ban.isPermanent()) {
            template = plugin.getConfig().getString("messages.ban-format",
                    "&c&lYOU ARE BANNED\n&7Reason: &f%reason%\n&7Ban ID: &f%banid%\n&7Banned by: &f%admin%");
        } else {
            template = plugin.getConfig().getString("messages.tempban-format",
                    "&c&lYOU ARE TEMPORARILY BANNED\n&7Reason: &f%reason%\n&7Expires: &f%expiry%\n&7Ban ID: &f%banid%\n&7Banned by: &f%admin%");
        }

        return ChatColor.translateAlternateColorCodes('&', template
                .replace("%reason%", ban.getReason())
                .replace("%banid%", ban.getBanId())
                .replace("%admin%", ban.getBannedBy())
                .replace("%expiry%", ban.getFormattedExpiry()));
    }
}
