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

import java.util.Date;

public class BanCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public BanCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> <reason>");
            return true;
        }

        String playerName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Get player (online or offline)
        Player target = Bukkit.getPlayer(playerName);
        String uuid;
        String ip = "Unknown";

        if (target != null) {
            // Player is online
            uuid = target.getUniqueId().toString();
            ip = target.getAddress().getAddress().getHostAddress();
        } else {
            // Try to get offline player
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            uuid = offlinePlayer.getUniqueId().toString();
        }

        // Generate ban ID
        String banId = apiClient.generateBanId();

        // Get admin name
        String adminName = sender instanceof Player ? sender.getName() : "Console";

        // Create ban object
        Ban ban = Ban.builder()
                .banId(banId)
                .playerUuid(uuid)
                .playerName(playerName)
                .ipAddress(ip)
                .reason(reason)
                .bannedBy(adminName)
                .timestamp(new Date())
                .expiry(null) // Permanent ban
                .active(true)
                .source("game")
                .build();

        // Save via API
        try {
            apiClient.createBan(ban);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to create ban: " + e.getMessage());
            return true;
        }

        // Kick player if online
        if (target != null && target.isOnline()) {
            String kickMessage = plugin.getConfig().getString("messages.ban-format", "&c&lYOU ARE BANNED")
                    .replace("%reason%", reason)
                    .replace("%banid%", banId)
                    .replace("%admin%", adminName);
            target.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));
        }

        // Send success message
        String successMessage = plugin.getConfig().getString("messages.ban-success", "&aSuccessfully banned %player%")
                .replace("%player%", playerName)
                .replace("%banid%", banId);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage));

        return true;
    }
}
