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

public class UnmuteCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public UnmuteCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.unmute")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        String targetName = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Resolve UUID
                String uuid = null;
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    uuid = target.getUniqueId().toString();
                } else {
                    // Fetch for offline player omitted for brevity, logic same as Unban
                    try {
                        uuid = apiClient.getActiveBanByUuid(targetName) != null ? null : null; // Placeholder
                        // For now we need UUID. If offline, use name lookup (not implemented in this
                        // snippet)
                        // Assuming target is online or we rely solely on name lookup if available
                    } catch (Exception e) {
                    }
                }

                // For simplicity in this iteration, we try to find active ban by iterating if
                // offline or use UUID if online
                // Implementing a proper lookup:
                if (target != null) {
                    uuid = target.getUniqueId().toString();
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Player must be online to be unmuted (Offline UUID lookup pending).");
                    // Note: You can implement Name -> UUID lookup here using Ashcon API like in
                    // frontend
                    return;
                }

                // Check if muted
                Ban activeMute = apiClient.getActiveBanByUuid(uuid, "MUTE");
                if (activeMute == null) {
                    sender.sendMessage(ChatColor.RED + "Player is not muted.");
                    return;
                }

                apiClient.deactivateBan(activeMute.getBanId());
                sender.sendMessage(ChatColor.GREEN + "Unmuted " + targetName);

                if (target != null) {
                    target.sendMessage(ChatColor.GREEN + "You have been unmuted.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Error unmuting player: " + e.getMessage());
            }
        });

        return true;
    }
}
