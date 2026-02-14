package com.ninesmp.bansystem.commands;

import com.ninesmp.bansystem.BanSystemPlugin;
import com.ninesmp.bansystem.api.ApiClient;
import com.ninesmp.bansystem.models.Ban;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCommand implements CommandExecutor {

    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public UnbanCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unban")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
            return true;
        }

        String targetName = args[0];

        // Run asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Try to find active ban by name (we might need to look up UUID first in a real
                // scenario,
                // but strictly speaking our API finds bans by UUID or IP.
                // For now, let's try to resolve UUID from name if possible, or just look up by
                // name if API supported it.
                // Since our API currently supports fetching active bans, we might need to
                // iterate or add a lookup by name endpoint.
                // HOWEVER, for now let's use the API to get all active bans and filter, or just
                // use UUID if player is known.

                // Better approach: Get offline player to get UUID
                // Note: getOfflinePlayer is deprecated by name but still widely used for this.
                // Ideally we'd use a UUID fetcher, but for simplicity:
                @SuppressWarnings("deprecation")
                String uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId().toString();

                // Check if banned
                Ban activeBan = apiClient.getActiveBanByUuid(uuid, "BAN");

                if (activeBan == null) {
                    sender.sendMessage(ChatColor.RED + "Player is not banned.");
                    return;
                }

                apiClient.deactivateBan(activeBan.getBanId());
                sender.sendMessage(ChatColor.GREEN + "Successfully unbanned " + targetName + ".");

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Error unbanning player: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }
}
