package com.firesoftitan.play.PluginKeeper.listeners;

import com.firesoftitan.play.PluginKeeper.Interface;
import com.firesoftitan.play.PluginKeeper.PluginKeeper;
import com.firesoftitan.play.PluginKeeper.managers.PluginKeeperManager;
import com.firesoftitan.play.PluginKeeper.tools.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MainListener implements Listener {

    private Plugin plugin;

    public MainListener(Plugin plugin) {
        this.plugin = plugin;
        registerEvents();
    }

    public void registerEvents() {
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }
    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        Interface interf = Interface.getInterface(player);
        if (interf != null) {
            if (interf.isWaiting()) {
                event.setCancelled(true);
                Plugin plugin = interf.getWaiting();

                PluginKeeperManager PKM = PluginKeeperManager.getPKM(plugin);
                interf.clearWaiting();
                if (event.getMessage().equalsIgnoreCase("c") || event.getMessage().equalsIgnoreCase("cancel")) {
                    new Interface(player);
                    return;
                }
                String id = Utilities.getID(event.getMessage());
                PKM.setId(id);
                player.sendMessage(ChatColor.GREEN + "[PluginKeeper]: id set for " + plugin.getDescription().getName() + ":" + id);
                if (id.equalsIgnoreCase("off"))
                {
                    String dowloadling = event.getMessage().replaceFirst("off ", "");
                    if (dowloadling.length() > 0) {
                        PKM.setDownloadlink(dowloadling);
                        player.sendMessage(ChatColor.GREEN + "[PluginKeeper]: download for " + plugin.getDescription().getName() + " set to " + dowloadling);
                    }
                }
                else {
                    PKM.reCallLive();
                }
                new Interface(player);
                return;

            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        int slot = event.getRawSlot();
        Interface.clickEvent(player, inventory, slot, event);
    }
    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("pluginkeeper.admin"))
        {
            List<String> needlink = new ArrayList<String>();
            for(Plugin plugin: Bukkit.getPluginManager().getPlugins())
            {
                PluginKeeperManager PKM = PluginKeeperManager.getPKM(plugin);
                if (PKM.getId() == null)
                {
                    needlink.add(plugin.getName());
                }
                else {
                    if (PKM.getId().equalsIgnoreCase("off")) {
                        if (!PKM.getMyVersion().equals(PKM.getLive()) || !plugin.isEnabled()) {
                            needlink.add(plugin.getName());
                            break;
                        }
                    }
                }
            }
            sendAdminMessage(player, needlink);

            new BukkitRunnable() {
                @Override
                public void run() {
                    sendAdminMessage(player, needlink);
                }
            }.runTaskLater(PluginKeeper.instance, 8*20);

        }

    }

    private void sendAdminMessage(Player player, List<String> needlink) {
        if (needlink.size() == 0)
        {
            player.sendMessage(ChatColor.GREEN + "[PluginKeeper]: All plugins up to date!");
        }
        else
        {
            if (needlink.size() == 1) {
                player.sendMessage(ChatColor.YELLOW + "[PluginKeeper]: The plugin " + ChatColor.WHITE + needlink.get(0) + ChatColor.YELLOW + " needs your attention!");
            }
            else
            {
                player.sendMessage(ChatColor.YELLOW + "[PluginKeeper]: There are " + ChatColor.WHITE + needlink.size() + ChatColor.YELLOW + " plugins that need your attention!");
            }
        }
    }
}
