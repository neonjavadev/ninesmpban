package com.ninesmp.bansystem.commands;

import com.ninesmp.bansystem.BanSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BanTabCompleter implements TabCompleter {
    private final BanSystemPlugin plugin;

    public BanTabCompleter(BanSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("ban") ||
                command.getName().equalsIgnoreCase("tempban") ||
                command.getName().equalsIgnoreCase("kick")) {

            if (args.length == 1) {
                // First argument: player name
                String partial = args[0].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            } else if (command.getName().equalsIgnoreCase("tempban") && args.length == 2) {
                // Second argument for tempban: time suggestions
                completions.add("1h");
                completions.add("6h");
                completions.add("12h");
                completions.add("1d");
                completions.add("3d");
                completions.add("7d");
                completions.add("30d");
            } else if (args.length == (command.getName().equalsIgnoreCase("tempban") ? 3 : 2)) {
                // Reason suggestions from config
                completions = plugin.getConfig().getStringList("ban-reasons");
            }
        }

        return completions;
    }
}
