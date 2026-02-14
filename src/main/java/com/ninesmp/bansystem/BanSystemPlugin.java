package com.ninesmp.bansystem;

import com.ninesmp.bansystem.api.ApiClient;
import com.ninesmp.bansystem.commands.BanCommand;
import com.ninesmp.bansystem.commands.BanTabCompleter;
import com.ninesmp.bansystem.commands.KickCommand;
import com.ninesmp.bansystem.commands.ReloadCommand;
import com.ninesmp.bansystem.commands.TempBanCommand;
import com.ninesmp.bansystem.commands.UnbanCommand;
import com.ninesmp.bansystem.listeners.AsyncPlayerChatListener;
import com.ninesmp.bansystem.listeners.PlayerJoinListener;
import com.ninesmp.bansystem.tasks.BanSyncTask;
import org.bukkit.plugin.java.JavaPlugin;

public class BanSystemPlugin extends JavaPlugin {
    private ApiClient apiClient;
    private BanSyncTask syncTask;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize API Client
        String apiUrl = getConfig().getString("api.url");
        String apiKey = getConfig().getString("api.key");

        apiClient = new ApiClient(apiUrl, apiKey, getLogger());

        // Register commands
        BanTabCompleter tabCompleter = new BanTabCompleter(this);
        getCommand("ban").setExecutor(new BanCommand(this, apiClient));
        getCommand("tempban").setExecutor(new TempBanCommand(this, apiClient));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this, apiClient));
        getCommand("banreload").setExecutor(new ReloadCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this, apiClient));
        getCommand("tempmute").setExecutor(new TempMuteCommand(this, apiClient));
        getCommand("unmute").setExecutor(new UnmuteCommand(this, apiClient));

        // Tab completers
        getCommand("ban").setTabCompleter(new BanTabCompleter(this));
        getCommand("tempban").setTabCompleter(new BanTabCompleter(this));
        getCommand("kick").setTabCompleter(new BanTabCompleter(this));
        getCommand("unban").setTabCompleter(new BanTabCompleter(this));
        getCommand("mute").setTabCompleter(new BanTabCompleter(this));
        getCommand("tempmute").setTabCompleter(new BanTabCompleter(this));
        getCommand("unmute").setTabCompleter(new BanTabCompleter(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, apiClient), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this, apiClient), this);

        // Start sync task
        int syncInterval = getConfig().getInt("sync.interval", 5) * 20; // Convert seconds to ticks
        syncTask = new BanSyncTask(this, apiClient);
        syncTask.runTaskTimerAsynchronously(this, 20L, syncInterval);

        getLogger().info("NineSMP Ban System enabled!");
        getLogger().info("Connected to API: " + apiUrl);
        getLogger().info("Cross-server sync running every " + (syncInterval / 20) + " seconds");
    }

    @Override
    public void onDisable() {
        // Cancel sync task
        if (syncTask != null) {
            syncTask.cancel();
        }

        getLogger().info("NineSMP Ban System disabled!");
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void reloadSystem() {
        reloadConfig();
        String apiUrl = getConfig().getString("api.url");
        String apiKey = getConfig().getString("api.key");
        apiClient.setCredentials(apiUrl, apiKey);

        // Restart sync task with new interval
        if (syncTask != null) {
            syncTask.cancel();
        }
        int syncInterval = getConfig().getInt("sync.interval", 5) * 20;
        syncTask = new BanSyncTask(this, apiClient);
        syncTask.runTaskTimerAsynchronously(this, 20L, syncInterval);

        getLogger().info("BanSystem reloaded. Connected to API: " + apiUrl);
    }
}
