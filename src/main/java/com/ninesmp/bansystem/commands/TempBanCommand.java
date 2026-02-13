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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempBanCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public TempBanCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempban <player> <time> <reason>");
            sender.sendMessage(ChatColor.GRAY + "Time format: 1s, 5m, 2h, 7d (seconds, minutes, hours, days)");
            return true;
        }

        String playerName = args[0];
        String timeString = args[1];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Parse time duration
        long durationMillis = parseTime(timeString);
        if (durationMillis <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid time format! Use: 1s, 5m, 2h, 7d");
            return true;
        }

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

        // Calculate expiry date
        Date expiry = new Date(System.currentTimeMillis() + durationMillis);

        // Create ban object
        Ban ban = Ban.builder()
                .banId(banId)
                .playerUuid(uuid)
                .playerName(playerName)
                .ipAddress(ip)
                .reason(reason)
                .bannedBy(adminName)
                .timestamp(new Date())
                .expiry(expiry)
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
            String kickMessage = plugin.getConfig()
                    .getString("messages.tempban-format", "&c&lYOU ARE TEMPORARILY BANNED")
                    .replace("%reason%", reason)
                    .replace("%banid%", banId)
                    .replace("%admin%", adminName)
                    .replace("%expiry%", expiry.toString());
            target.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));
        }

        // Send success message
        String successMessage = plugin.getConfig()
                .getString("messages.tempban-success", "&aSuccessfully temp-banned %player%")
                .replace("%player%", playerName)
                .replace("%banid%", banId)
                .replace("%duration%", timeString);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage));

        return true;
    }

    /**
     * Parse time string like "1d", "2h", "30m", "60s" to milliseconds
     */
    private long parseTime(String timeString) {
        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        if (!matcher.matches()) {
            return -1;
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        switch (unit) {
            case "s":
                return TimeUnit.SECONDS.toMillis(amount);
            case "m":
                return TimeUnit.MINUTES.toMillis(amount);
            case "h":
                return TimeUnit.HOURS.toMillis(amount);
            case "d":
                return TimeUnit.DAYS.toMillis(amount);
            default:
                return -1;
        }
    }
}
