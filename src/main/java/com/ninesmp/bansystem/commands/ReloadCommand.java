package com.ninesmp.bansystem.commands;

import com.ninesmp.bansystem.BanSystemPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final BanSystemPlugin plugin;

    public ReloadCommand(BanSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bansystem.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        plugin.reloadSystem();
        sender.sendMessage(ChatColor.GREEN + "BanSystem configuration reloaded!");
        return true;
    }
}
