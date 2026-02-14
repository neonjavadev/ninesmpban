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

import java.util.Arrays;
import java.util.Date;

public class MuteCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public MuteCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.mute")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> <reason>");
            return true;
        }

        String targetName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Asynchronously process the mute
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Resolve UUID (simplified for now, ideally use a fetcher)
                String uuid = null;
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    uuid = target.getUniqueId().toString();
                } else {
                    // Try to fetch from API/Cache if offline (omitted for brevity, assumes online
                    // or known)
                    sender.sendMessage(ChatColor.RED
                            + "Player must be online or known (Offline mute support not fully implemented yet).");
                    return;
                }

                // Check if already muted
                if (apiClient.getActiveBanByUuid(uuid, "MUTE") != null) {
                    sender.sendMessage(ChatColor.RED + "Player is already muted.");
                    return;
                }

                String banId = apiClient.generateBanId();
                Ban ban = Ban.builder()
                        .banId(banId)
                        .playerUuid(uuid)
                        .playerName(targetName)
                        .reason(reason)
                        .bannedBy(sender.getName())
                        .timestamp(new Date())
                        .expiry(null) // Permanent
                        .active(true)
                        .source("game")
                        .type("MUTE")
                        .build();

                apiClient.createBan(ban);

                sender.sendMessage(ChatColor.GREEN + "Muted " + targetName + " for: " + reason);

                // Broadcast
                String broadcast = ChatColor.GOLD + "user " + targetName + " was muted for " + reason
                        + " for Permanent";
                Bukkit.broadcastMessage(broadcast);

                if (target != null) {
                    target.sendMessage(ChatColor.RED + "You have been muted for: " + reason);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Error muting player: " + e.getMessage());
            }
        });

        return true;
    }
}
