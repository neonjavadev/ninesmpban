package com.ninesmp.bansystem.tasks;

import com.ninesmp.bansystem.BanSystemPlugin;
import com.ninesmp.bansystem.api.ApiClient;
import com.ninesmp.bansystem.models.Ban;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BanSyncTask extends BukkitRunnable {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public BanSyncTask(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public void run() {
        // Check for expired bans
        apiClient.checkExpiredBans();

        // Get all active bans
        List<Ban> activeBans = apiClient.getAllActiveBans();

        // Check online players against active bans
        for (Player player : Bukkit.getOnlinePlayers()) {
            String uuid = player.getUniqueId().toString();
            String ip = player.getAddress().getAddress().getHostAddress();

            // Check if player should be banned
            for (Ban ban : activeBans) {
                if (ban.getPlayerUuid().equals(uuid) || ban.getIpAddress().equals(ip)) {
                    // Player is banned, kick them
                    String kickMessage = formatBanMessage(ban);
                    player.kickPlayer(kickMessage);
                    plugin.getLogger().info("Kicked " + player.getName() + " due to active ban: " + ban.getBanId());
                    break;
                }
            }
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
