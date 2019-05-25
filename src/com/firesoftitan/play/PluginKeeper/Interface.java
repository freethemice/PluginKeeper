package com.firesoftitan.play.PluginKeeper;

import com.firesoftitan.play.PluginKeeper.managers.PluginKeeperManager;
import com.firesoftitan.play.PluginKeeper.tools.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Interface {
    private static HashMap<UUID, Interface> interfaces = new HashMap<UUID, Interface>();
    private Inventory inventory = null;
    private Player player = null;
    private Plugin[] plugins;
    private Plugin waiting = null;
    public Interface(Player player)
    {
        this.player = player;
        build();
        this.player.openInventory(this.inventory);
        interfaces.put(player.getUniqueId(), this);
    }
    public static Interface getInterface(Player player)
    {
        return interfaces.get(player.getUniqueId());
    }
    private void build()
    {
        inventory = Bukkit.createInventory(null, 54, "My Plugin Keeper");
        plugins = Bukkit.getPluginManager().getPlugins();
        int i = 0;
        int selector = 0;
        int slot = 0;
        for(Plugin plugin: plugins)
        {
            if (i >= selector && i < 54) {
                PluginKeeperManager PKM = PluginKeeperManager.getPKM(plugin);

                ItemStack icon;
                List<String> lore = new ArrayList<String>();
                lore.add(ChatColor.GRAY + "Shift+Right Click to edit plugin id");
                if (PKM.getId() == null)
                {
                    icon = new ItemStack(Material.YELLOW_SHULKER_BOX);
                    icon = Utilities.changeName(icon, ChatColor.GOLD + "Plugin: " + ChatColor.WHITE + PKM.getName());
                    lore.add(ChatColor.RED + "Plugin is not setup");
                    lore.add(ChatColor.WHITE + "Click here to enter id in chat.");
                } else {
                    icon = new ItemStack(Material.GREEN_SHULKER_BOX);
                    icon = Utilities.changeName(icon, ChatColor.GOLD + "Plugin: " + ChatColor.WHITE + PKM.getPlugin().getName() + ChatColor.GOLD + ":" + ChatColor.WHITE +  PKM.getId());
                }
                if (PKM.getId() != null) {
                    if (PKM.getId().equals("off"))
                    {
                        icon = new ItemStack(Material.WHITE_SHULKER_BOX);
                        icon = Utilities.changeName(icon, ChatColor.GOLD + "Plugin: " + ChatColor.WHITE + PKM.getPlugin().getName() + ChatColor.GOLD);
                        lore.add(ChatColor.RED + "Plugin check is turned off!");
                        lore.add(ChatColor.WHITE + "Click for download link");
                    }
                    else {
                        if (PKM.getLive() == null) {
                            lore.add(ChatColor.YELLOW + "Needs versions check");
                            lore.add(ChatColor.WHITE + "Click here to check plugins version.");
                        } else {
                            if (PKM.getMyVersion().equals(PKM.getLive()) && plugin.isEnabled()) {
                                lore.add(ChatColor.GREEN + "Plugin is up to date.");
                            } else {
                                if (!plugin.isEnabled()) {
                                    icon = new ItemStack(Material.GRAY_SHULKER_BOX);
                                    icon = Utilities.changeName(icon, ChatColor.GOLD + "Plugin: " + ChatColor.WHITE + PKM.getName() + ChatColor.GOLD + ":" + ChatColor.WHITE + PKM.getId());
                                    lore.add(ChatColor.RED + "Plugin is disabled!");
                                } else {
                                    icon = new ItemStack(Material.RED_SHULKER_BOX);
                                    icon = Utilities.changeName(icon, ChatColor.GOLD + "Plugin: " + ChatColor.WHITE + PKM.getName() + ChatColor.GOLD + ":" + ChatColor.WHITE + PKM.getId());
                                }
                                lore.add(ChatColor.YELLOW + "Plugin needs updating.");
                                lore.add(ChatColor.WHITE + "Click here to get download link.");
                            }
                        }
                    }
                }

                icon = Utilities.addLore(icon, lore);

                inventory.setItem(slot, icon.clone());
                slot++;
            }
            i++;
        }
    }
    public boolean isWaiting()
    {
        return waiting != null;
    }

    public Plugin getWaiting() {
        return waiting;
    }
    public void clearWaiting()
    {
        waiting = null;
    }

    public static void clickEvent(Player player, Inventory inventory, int slot, InventoryClickEvent event)
    {
        Interface interf = Interface.getInterface(player);
        if (interf != null)
        {
            if (inventory.equals(interf.inventory))
            {
                event.setCancelled(true);
                if (slot > -1 && slot < 54)
                {
                    if (slot < interf.plugins.length)
                    {
                        Plugin plugin = interf.plugins[slot];
                        PluginKeeperManager PKM = PluginKeeperManager.getPKM(plugin);
                        if (PKM.getId() == null || event.getClick() == ClickType.SHIFT_RIGHT)
                        {
                            interf.waiting = plugin;
                            player.sendMessage(ChatColor.GREEN + "Please enter plugin id or plugin spigot link in chat.");
                            player.sendMessage(ChatColor.RED + PKM.getName());
                            player.sendMessage("https://www.google.com/search?q=spigot+" + PKM.getName());
                            player.sendMessage("https://www.google.com/search?q=spigot+" + PKM.getPlugin().getDescription().getFullName().replace(" ", "+"));
                            player.sendMessage(ChatColor.AQUA + "This ID is the last few numbers at the end of the spigot link.");
                            player.sendMessage(ChatColor.WHITE + "Enter off to turn off plugin check for this plugin. You can add a download link after off, if you want.");
                            player.sendMessage(ChatColor.GRAY + "Enter c or cancel to Cancel.");
                            player.closeInventory();
                        }
                        else
                        {
                            if (PKM.getLive() == null && !PKM.getId().equalsIgnoreCase("off"))
                            {
                                PKM.reCallLive();
                                player.sendMessage(ChatColor.GREEN + "Checking version now...");
                            }
                            else
                            {
                                player.sendMessage(ChatColor.AQUA + PKM.getDownloadlink());
                            }
                        }
                        player.closeInventory();
                    }
                }
            }
        }
    }

}
