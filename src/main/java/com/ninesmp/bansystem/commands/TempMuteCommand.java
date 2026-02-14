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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempMuteCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;
    private final ApiClient apiClient;

    public TempMuteCommand(BanSystemPlugin plugin, ApiClient apiClient) {
        this.plugin = plugin;
        this.apiClient = apiClient;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.tempmute")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempmute <player> <duration> <reason>");
            return true;
        }

        String targetName = args[0];
        String durationStr = args[1];
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        long duration = parseDuration(durationStr);
        if (duration <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format. Use 10s, 10m, 10h, 1d.");
            return true;
        }

        Date expiry = new Date(System.currentTimeMillis() + duration);

        // Asynchronously process the mute
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Resolve UUID
                String uuid = null;
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    uuid = target.getUniqueId().toString();
                } else {
                    sender.sendMessage(ChatColor.RED + "Player must be online or known.");
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
                        .expiry(expiry)
                        .active(true)
                        .source("game")
                        .type("MUTE")
                        .build();

                apiClient.createBan(ban);

                sender.sendMessage(ChatColor.GREEN + "Temp-Muted " + targetName + " for: " + reason);

                // Broadcast
                String broadcast = ChatColor.GOLD + "user " + targetName + " was muted for " + reason + " for "
                        + durationStr;
                Bukkit.broadcastMessage(broadcast);

                if (target != null) {
                    target.sendMessage(ChatColor.RED + "You have been muted for " + durationStr + " for: " + reason);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Error muting player: " + e.getMessage());
            }
        });

        return true;
    }

    private long parseDuration(String duration) {
        try {
            long totalMillis = 0;
            Pattern pattern = Pattern.compile("(\\d+)([smhd])");
            Matcher matcher = pattern.matcher(duration.toLowerCase());

            while (matcher.find()) {
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);

                switch (unit) {
                    case "s":
                        totalMillis += value * 1000L;
                        break;
                    case "m":
                        totalMillis += value * 60000L;
                        break;
                    case "h":
                        totalMillis += value * 3600000L;
                        break;
                    case "d":
                        totalMillis += value * 86400000L;
                        break;
                }
            }

            return totalMillis;
        } catch (Exception e) {
            return -1;
        }
    }
}
