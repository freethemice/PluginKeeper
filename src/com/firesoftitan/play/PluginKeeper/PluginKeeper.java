package com.firesoftitan.play.PluginKeeper;

import com.firesoftitan.play.PluginKeeper.listeners.MainListener;
import com.firesoftitan.play.PluginKeeper.managers.PluginKeeperManager;
import com.firesoftitan.play.PluginKeeper.tools.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class PluginKeeper extends JavaPlugin {

    //private Connection connection;
    public static PluginKeeper instance;
    public static MainListener listener;
    public static UpdateChecker updateChecker;
    private File configFile;
    private FileConfiguration config;

    public PluginKeeper() {

    }

    public void onDisable() {
        try {
            PluginKeeperManager.save();
            config.save(configFile);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public void onEnable() {
        instance = this;
        this.configFile = new File("plugins/" + instance.getDescription().getName().replace(" ", "_") + "/config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);

        if (!this.config.contains("options.setting")) {
            this.config.set("options.setting", "something");
        }
        try {
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String setting = this.config.getString("options.setting");

        listener = new MainListener(this);


        System.out.println("[PluginKeeper]: Initialized and Enabled.");

        updateChecker = new UpdateChecker(this, "65264");
        updateChecker.runTaskLater(this, 3 * 20);

        new BukkitRunnable()
        {

            @Override
            public void run() {
                Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
                for(Plugin plugin: plugins)
                {
                    PluginKeeperManager keeperManager = new PluginKeeperManager(plugin);
                }
            }
        }.runTaskLater(this, 7*20);

    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
                if (sender.hasPermission("pluginkeeper.admin")) {
                    if (label.equalsIgnoreCase("pluginkeeper") || label.equalsIgnoreCase("pk")) {
                        new Interface((Player) sender);
                        return true;
                    }
                }


        } catch (Exception e) {

        }
        return false;
    }
}
