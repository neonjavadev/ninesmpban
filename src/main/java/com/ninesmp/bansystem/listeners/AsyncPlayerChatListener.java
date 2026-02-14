package com.ninesmp.bansystem.listeners;

import com.ninesmp.bansystem.BanSystemPlugin;
import com.ninesmp.bansystem.api.ApiClient;
import com.ninesmp.bansystem.models.Ban;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class AsyncPlayerChatListener implements Listener {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public AsyncPlayerChatListener(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ip = player.getAddress().getAddress().getHostAddress();

        // Check for active punishments
        List<Ban> activeBans = apiClient.getAllActiveBans();

        for (Ban ban : activeBans) {
            // Only care about MUTE type
            if (ban.getType() == null || !ban.getType().equals("MUTE")) {
                continue;
            }

            // Check if this ban applies to the player
            if (ban.getPlayerUuid().equals(uuid) || ban.getIpAddress().equals(ip)) {
                // Player is muted
                event.setCancelled(true);

                String message = plugin.getConfig().getString("messages.mute-format",
                        "&c&lYOU ARE MUTED\n&7Reason: &f%reason%\n&7Expires: &f%timeleft%\n&7Ban ID: &f%banid%");

                message = ChatColor.translateAlternateColorCodes('&', message
                        .replace("%reason%", ban.getReason())
                        .replace("%banid%", ban.getBanId())
                        .replace("%timeleft%", ban.getTimeLeft()));

                player.sendMessage(message);
                return;
            }
        }
    }
}
