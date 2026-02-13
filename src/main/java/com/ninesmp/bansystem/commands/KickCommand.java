package com.ninesmp.bansystem.commands;

import com.ninesmp.bansystem.BanSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {
    private final BanSystemPlugin plugin;

    public KickCommand(BanSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /kick <player> <reason>");
            return true;
        }

        String playerName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Get player
        Player target = Bukkit.getPlayer(playerName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online!");
            return true;
        }

        // Kick player
        String kickMessage = ChatColor.translateAlternateColorCodes('&',
                "&c&lYou were kicked from survival: Connection Lost\n\n" +
                        "&cYou were kicked from lobby: " + reason);

        target.kickPlayer(kickMessage);

        // Send success message
        String successMessage = plugin.getConfig().getString("messages.kick-success", "&aSuccessfully kicked %player%")
                .replace("%player%", playerName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage));

        return true;
    }
}
