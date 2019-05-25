package com.firesoftitan.play.PluginKeeper.managers;

import com.firesoftitan.play.PluginKeeper.PluginKeeper;
import com.firesoftitan.play.PluginKeeper.tools.VersionResults;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PluginKeeperManager {
    private static File configFile = new File("plugins/" + PluginKeeper.instance.getDescription().getName().replace(" ", "_") + "/plugins.yml");
    private static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    private static HashMap<String, PluginKeeperManager> plugins = new HashMap<String, PluginKeeperManager>();
    private String live = null;
    private String livelast = null;
    private Plugin plugin = null;
    private String id = null;
    private String downloadlink = null;
    public PluginKeeperManager(Plugin plugin)
    {
        this.plugin = plugin;
        id = PluginKeeperManager.config.getString("plugins." + getName() + ".id"); // 00000
        livelast = PluginKeeperManager.config.getString("plugins." + getName() + ".version.live"); //two
        downloadlink = PluginKeeperManager.config.getString("plugins." + getName() + ".downloadlink"); //two
        if (plugin == PluginKeeper.instance && this.id == null)
        {
            this.setId("00000");
        }
        reCallLive();
        PluginKeeperManager.plugins.put(getName(), this);

    }

    public String getDownloadlink() {
        if (id == null) return null;
        if (id.equalsIgnoreCase("off")) return downloadlink;
        return PluginKeeper.updateChecker.getDownload()  + this.getId();
    }

    public void setDownloadlink(String downloadlink) {
        this.downloadlink = downloadlink;
        PluginKeeperManager.config.set("plugins." + getName() + ".downloadlink", downloadlink);
        save();
    }

    public static PluginKeeperManager getPKM(Plugin plugin)
    {
        return getPKM(plugin.getDescription().getName());
    }
    public static PluginKeeperManager getPKM(String name)
    {
        return plugins.get(name);
    }

    public String getId() {
        return this.id;
    }

    public void reCallLive()
    {
        if (this.id != null) {
            PluginKeeper.updateChecker.getPluginVersionLive(new VersionResults() {
                @Override
                public void onResult(String version) {
                    if (version != null) {
                        live = version;
                        PluginKeeperManager.config.set("plugins." + getName() + ".version.live", live);
                    }
                }
            }, this.id);
        }
    }
    public void setId(String id)
    {
        this.id = id;
        PluginKeeperManager.config.set("plugins." + getName() + ".id", id);
        save();
    }
    public static void save()
    {
        try {
            PluginKeeperManager.config.save(PluginKeeperManager.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getMyVersion()
    {
        return plugin.getDescription().getVersion();
    }
    public String getName() {
        return plugin.getDescription().getName();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getLive() {
        return live;
    }

    public String getLivelast() {
        return livelast;
    }
}
