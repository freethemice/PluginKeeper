package com.firesoftitan.play.PluginKeeper.tools;

import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

public class Utilities {
    private static HashMap<String, Boolean> loadedSQLs = new HashMap<String, Boolean>();
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private Utilities() {

    }

    public static boolean areEqual(Location location1, Location location2) {
        if (location1.getWorld().getName().equals(location2.getWorld().getName())) {
            if (location1.getBlockX() == location2.getBlockX()) {
                if (location1.getBlockY() == location2.getBlockY()) {
                    if (location1.getBlockZ() == location2.getBlockZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String serializeLocation(Location l) {
        return l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ();
    }

    public static Location deserializeLocation(String l) {
        try {
            World w = Bukkit.getWorld(l.split(";")[0]);
            if (w != null)
                return new Location(w, Integer.parseInt(l.split(";")[1]), Integer.parseInt(l.split(";")[2]), Integer.parseInt(l.split(";")[3]));
        } catch (NumberFormatException x) {
        }
        return null;
    }

    public static boolean isLoaded(Location loc) {
        if (loc == null) return false;
        World world = loc.getWorld();
        if (world.isChunkLoaded((int) loc.getBlockX() >> 4, (int) loc.getBlockZ() >> 4)) {
            return true;
        }
        return false;
    }

    public static Location getRandomLocation(World world, int size) {
        Random letsGo = new Random(System.currentTimeMillis());
        double x = letsGo.nextInt(size * 2);
        double y = 300;
        double z = letsGo.nextInt(size * 2);
        x = x - size;
        z = z - size;
        Location location = new Location(world, x, y, z);
        y = world.getHighestBlockYAt(location);
        Location location1 = new Location(world, x, y, z);
        if (location1.clone().add(0, -1, 0).getBlock().getType() == Material.WATER || location1.clone().add(0, -1, 0).getBlock().getType() == Material.LAVA) {
            return getRandomLocation(world, size);
        }
        return location1.clone();
    }



    public static String encode(Location location) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i.x", location.getX());
        config.set("i.y", location.getY());
        config.set("i.z", location.getZ());
        config.set("i.pitch", location.getPitch() + "");
        config.set("i.yaw", location.getYaw() + "");
        if (location.getWorld() == null) {
            config.set("i.world", "worldmain");
        } else {
            config.set("i.world", location.getWorld().getName());
        }
        return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an {@link Location} from a Base64 String
     *
     * @param string Base64 encoded String to decode
     * @return Decoded {@link Location}
     */
    public static Location decodeLocation(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        double x = config.getDouble("i.x");
        double y = config.getDouble("i.y");
        double z = config.getDouble("i.z");
        float pitch = Float.valueOf(config.getString("i.pitch"));
        float yaw = Float.valueOf(config.getString("i.yaw"));
        String worldname = config.getString("i.world");
        World world = Bukkit.getWorld(worldname);
        Location location = new Location(world, x, y, z, yaw, pitch);
        return location.clone();
    }

    public static boolean isAir(Material material) {
        if (material == Material.AIR || material == Material.CAVE_AIR) {
            return true;
        }
        return false;
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatCommas(Long value) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String numberAsString = numberFormat.format(value);
        return numberAsString;
    }

    public static String formatCommas(double value) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String numberAsString = numberFormat.format(value);
        return numberAsString;
    }

    public static String formatTime(long lastping) {
        String time = " Seconds";
        lastping = lastping / 1000;
        if (lastping > 60) {
            time = " Minutes";
            lastping = lastping / 60;
            if (lastping > 60) {
                time = " Hours";
                lastping = lastping / 60;
                if (lastping > 24) {
                    time = " Days";
                    lastping = lastping / 24;
                    if (lastping > 30) {
                        time = " Months";
                        lastping = lastping / 24;
                    }
                }
            }
        }
        return lastping + time;
    }

    public static String convertToTimePasted(long lastping) {
        long last = System.currentTimeMillis() - lastping;
        String time = " Seconds";
        last = last / 1000;
        if (last > 60) {
            time = " Minutes";
            last = last / 60;
            if (last > 60) {
                time = " Hours";
                last = last / 60;
            }
        }
        return last + time;
    }

    public static ItemStack addItemsToPlayer(Player player, ItemStack placing) {
        placing = placing.clone();
        List<Integer> emptySlos = new ArrayList<Integer>();
        Inventory playersInv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack checkItem = playersInv.getItem(i);
            if (isEmpty(checkItem)) {
                emptySlos.add(i); //this slot is empty lets keep that in mind for later
            } else {
                checkItem = checkItem.clone();
                if (isItemEqual(checkItem, placing))// is this the same thing
                {
                    if (checkItem.getAmount() < checkItem.getMaxStackSize()) //is there room in this stack for more
                    {
                        int placeAmount = Math.min(checkItem.getMaxStackSize() - checkItem.getAmount(), placing.getAmount()); //how much more can it hold
                        checkItem.setAmount(checkItem.getAmount() + placeAmount); //uppdate the item
                        playersInv.setItem(i, checkItem.clone());//place the item back
                        if (placing.getAmount() - placeAmount <= 0) //is there any left over we need to place
                        {
                            return null; //we are done
                        }
                        placing.setAmount(placing.getAmount() - placeAmount); //some was left, lets keep looking
                    }
                }
            }

        }
        //we finished looking for more room now lets fill in the empty slots
        for (Integer slot : emptySlos) {
            int howMuch = Math.min(placing.getMaxStackSize(), placing.getAmount());
            ItemStack itemStack = placing.clone();
            itemStack.setAmount(howMuch);
            playersInv.setItem(slot, itemStack.clone());
            if (placing.getAmount() - howMuch <= 0) //is there any left over we need to place
            {
                return null; //we are done
            }
            placing.setAmount(placing.getAmount() - howMuch); //some was left, lets keep looking
        }

        return placing.clone(); // didn't have enough room, sorry

    }

    public static ItemStack getItemStackFromBlock(Location block) {
        return getItemStackFromBlock(block.getBlock());
    }

    public static ItemStack getItemStackFromBlock(Block block) {
        ItemStack test = block.getState().getData().toItemStack(1);
        return test.clone();
    }

    public static void checkDeathItem(Player player, PlayerInventory inventory, int slot) {
        if (hasDeathItem(player, inventory, slot)) {
            player.setHealth(15);
            player.setFoodLevel(10);
        }
    }

    public static boolean isDeathItem(ItemStack itemStack) {
        if (!Utilities.isEmpty(itemStack)) {
            if (itemStack.getType() == Material.FEATHER) {
                String name = Utilities.getName(itemStack, false);
                if (name.equals(ChatColor.translateAlternateColorCodes('&', "&6Death Feather"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasDeathItem(Player player, PlayerInventory inventory, int slot) {
        if (isDeathItem(inventory.getItem(slot))) {
            return true;
        }
        return false;
    }

    public static HashMap<Integer, Integer> getGUISlotFromRealSlot(Block target) {
        HashMap<Integer, Integer> trans = new HashMap<Integer, Integer>();
        switch (target.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
                Inventory inventory = Utilities.getVanillaInventoryFor(target);
                if (inventory.getHolder() instanceof DoubleChest) {
                    for (int i = 0; i < 54; i++) {
                        trans.put(i, i);
                    }
                } else {
                    for (int i = 0; i < 27; i++) {
                        trans.put(i, i);
                    }
                }
                return trans;
            case DISPENSER:
                // 12 13 14
                // 21 22 23
                // 30 31 32
                trans.put(0, 12);
                trans.put(1, 13);
                trans.put(2, 14);
                trans.put(3, 21);
                trans.put(4, 22);
                trans.put(5, 23);
                trans.put(6, 30);
                trans.put(7, 31);
                trans.put(8, 32);
                return trans;
            case HOPPER:
            case DROPPER:
                trans.put(0, 20);
                trans.put(1, 21);
                trans.put(2, 22);
                trans.put(3, 23);
                trans.put(4, 24);
                return trans;
            case FURNACE:
                trans.put(0, 12);
                trans.put(1, 30);
                trans.put(2, 23);
                return trans;
            case BREWING_STAND:
                trans.put(0, 30);//10 13 30 31 32
                trans.put(1, 31);
                trans.put(2, 32);
                trans.put(3, 10);
                trans.put(4, 13);
                return trans;
            default:
                return null;
        }
    }

    public static HashMap<Integer, Integer> getRealSlotFromGUISlot(Block target) {
        HashMap<Integer, Integer> trans = new HashMap<Integer, Integer>();
        switch (target.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
                Inventory inventory = Utilities.getVanillaInventoryFor(target);
                if (inventory.getHolder() instanceof DoubleChest) {
                    for (int i = 0; i < 54; i++) {
                        trans.put(i, i);
                    }
                } else {
                    for (int i = 0; i < 27; i++) {
                        trans.put(i, i);
                    }
                }
                return trans;
            case DISPENSER:
                // 12 13 14
                // 21 22 23
                // 30 31 32
                trans.put(12, 0);
                trans.put(13, 1);
                trans.put(14, 2);
                trans.put(21, 3);
                trans.put(22, 4);
                trans.put(23, 5);
                trans.put(30, 6);
                trans.put(31, 7);
                trans.put(32, 8);
                return trans;
            case HOPPER:
            case DROPPER:
                trans.put(20, 0);
                trans.put(21, 1);
                trans.put(22, 2);
                trans.put(23, 3);
                trans.put(24, 4);
                return trans;
            case FURNACE:
                trans.put(12, 0);
                trans.put(30, 1);
                trans.put(23, 2);
                return trans;
            case BREWING_STAND:
                trans.put(30, 0); //10 13 30 31 32
                trans.put(31, 1);
                trans.put(32, 2);
                trans.put(13, 3);
                trans.put(10, 4);
                return trans;
            default:
                return null;
        }
    }

    /**
     * Get the vanilla inventory for the given block.
     *
     * @param target the block containing the target inventory
     * @return the block's inventory, or null if the block does not have one
     */
    public static Inventory getVanillaInventoryFor(Block target) {
        Chest c;
        switch (target.getType()) {
            case TRAPPED_CHEST:
            case CHEST:
                c = (Chest) target.getState();
                if (c.getInventory().getHolder() instanceof DoubleChest) {
                    DoubleChest dc = (DoubleChest) c.getInventory().getHolder();
                    return dc.getInventory();
                } else {
                    return c.getBlockInventory();
                }
            case DISPENSER:
            case HOPPER:
            case DROPPER:
            case FURNACE:
            case BREWING_STAND:
                //case BURNING_FURNACE:
                return ((InventoryHolder) target.getState()).getInventory();
            // any other vanilla inventory types ?
            default:
                return null;
        }
    }

    public static ItemStack changeName(ItemStack toAdd, String Name) {
        ItemMeta IM = toAdd.getItemMeta();
        IM.setDisplayName(Name);
        toAdd.setItemMeta(IM);
        return toAdd.clone();
    }

    public static boolean hasLore(ItemStack toAdd) {
        ItemMeta ITM = toAdd.getItemMeta();
        if (ITM != null) {
            if (ITM.hasLore()) {
                return true;
            }
        }

        return false;
    }

    public static List<String> getLore(ItemStack toAdd) {
        ItemMeta ITM = getItemMeta(toAdd);

        if (ITM.hasLore()) {
            return ITM.getLore();
        }

        return null;
    }

    public static int getLoreSize(ItemStack toAdd) {
        ItemMeta ITM = getItemMeta(toAdd);

        if (ITM.hasLore()) {
            return ITM.getLore().size();
        }

        return -1;
    }

    public static boolean isEmpty(ItemStack toCheck) {
        if (toCheck == null) {
            return true;
        }
        if (toCheck.getType().equals(Material.AIR)) {
            return true;
        }
        if (toCheck.getAmount() < 1) {
            return true;
        }
        return false;
    }

    public static ItemStack removeLore(ItemStack toAdd, int line) {
        ItemMeta ITM = getItemMeta(toAdd);
        if (ITM.hasLore()) {
            List<String> lore = ITM.getLore();
            lore.remove(line);
            ITM.setLore(lore);
            toAdd.setItemMeta(ITM.clone());
            return toAdd;
        }

        return toAdd.clone();
    }

    public static ItemStack addLore(boolean clear, ItemStack toAdd, List<String> lore) {

        ItemMeta ITM = getItemMeta(toAdd);

        if (!clear) {
            if (ITM.hasLore()) {
                List<String> lore2 = new ArrayList<String>();
                lore2.addAll(ITM.getLore());
                lore2.addAll(lore);
                lore.clear();
                lore = lore2;
            }
        }
        ITM.setLore(lore);
        toAdd.setItemMeta(ITM.clone());
        return toAdd;
    }

    public static String fixCapitalization(String Namespace) {
        if (Namespace.length() > 0) {
            String fixing = Namespace.replace("_", " ").toLowerCase();
            return WordUtils.capitalize(fixing);
        }
        return "";
    }

    public static boolean checkAlterProbe(ItemStack itemStack) {
        try {
            String name = Utilities.getName(itemStack, false);
            if (name.contains(ChatColor.DARK_PURPLE + "" + ChatColor.LIGHT_PURPLE + "ALTAR " + ChatColor.DARK_AQUA + "Probe - ")) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean hasNBTTag(ItemStack itemStack, String key) {
        NBTTagCompound nbtTagCompound = getNBTTag(itemStack);
        if (nbtTagCompound != null) {
            if (nbtTagCompound.hasKey(key)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack removeNBTTag(ItemStack itemStack, String key) {
        NBTTagCompound nbtTagCompound = getNBTTag(itemStack);
        if (nbtTagCompound != null) {
            if (nbtTagCompound.hasKey(key)) {
                if (nbtTagCompound.getKeys().size() == 1) {
                    return clearNBTTag(itemStack);
                } else {
                    nbtTagCompound.set(key, null);
                    return setNBTTag(itemStack, nbtTagCompound);
                }
            }
        }
        return itemStack;
    }

    public static ItemStack clearNBTTag(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_14_R1.ItemStack itemStack1 = CraftItemStack.asNMSCopy(itemStack);
            itemStack1.setTag(null);
            return CraftItemStack.asBukkitCopy(itemStack1);
        } catch (Exception E) {
            E.printStackTrace();
            return null;
        }
    }

    public static ItemStack setNBTTag(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        try {
            net.minecraft.server.v1_14_R1.ItemStack itemStack1 = CraftItemStack.asNMSCopy(itemStack);
            itemStack1.setTag(nbtTagCompound);
            return CraftItemStack.asBukkitCopy(itemStack1);
        } catch (Exception E) {
            E.printStackTrace();
            return null;
        }
    }

    public static NBTTagCompound getNBTTag(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_14_R1.ItemStack itemStack1 = CraftItemStack.asNMSCopy(itemStack);
            if (itemStack1.getTag() == null) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                return nbtTagCompound;
            }
            return itemStack1.getTag();
        } catch (Exception E) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            return nbtTagCompound;
        }
    }

    public static ItemStack addLore(ItemStack toAdd, List<String> lore) {
        return addLore(false, toAdd, lore);
    }
    public static boolean isNumber(String number)
    {
        try
        {
            int i = Integer.parseInt(number);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    public static String getID(String idFinder)
    {
        if (idFinder.toLowerCase().startsWith("off")) return "off";
        if (Utilities.isNumber(idFinder)) return idFinder;
        if (idFinder.endsWith("/"))
        {
            idFinder = idFinder.substring(0, idFinder.length() - 1);
        }
        for (int i = 1; i < idFinder.length(); i++)
        {
            String check = idFinder.substring(idFinder.length() - i, idFinder.length());
            if (!Utilities.isNumber(check))
            {
                i--;
                return idFinder.substring(idFinder.length() - i, idFinder.length());
            }
        }
        return null;
    }
    public static ItemStack addLore(boolean clear, ItemStack toAdd, String... lores) {
        List<String> lore = new ArrayList<String>();
        for (String l : lores) {
            lore.add(l);
        }
        toAdd = addLore(clear, toAdd, lore);
        return toAdd.clone();
    }

    public static ItemStack addLore(ItemStack toAdd, String... lores) {
        return addLore(false, toAdd, lores);
    }

    public static ItemStack clearLore(ItemStack toAdd) {
        List<String> lore = new ArrayList<String>();
        ItemMeta ITM = getItemMeta(toAdd);

        ITM.setLore(lore);
        toAdd.setItemMeta(ITM.clone());
        return toAdd;
    }

    public static ItemMeta getItemMeta(ItemStack toAdd) {
        if (toAdd.hasItemMeta()) {
            return toAdd.getItemMeta();
        }
        return Bukkit.getItemFactory().getItemMeta(toAdd.getType());

    }

    public static ItemStack clearEnchanents(ItemStack toAdd) {
        Set<Enchantment> all = toAdd.getEnchantments().keySet();
        for (Enchantment enc : all) {
            toAdd.removeEnchantment(enc);
        }
        return toAdd;
    }

    public static boolean equalsLore(List<String> lore, List<String> lore2) {
        String string1 = "";
        String string2 = "";
        Iterator var4 = lore.iterator();

        String string;
        while (var4.hasNext()) {
            string = (String) var4.next();
            string1 = string1 + "-NEW LINE-" + string;
        }

        var4 = lore2.iterator();

        while (var4.hasNext()) {
            string = (String) var4.next();
            string2 = string2 + "-NEW LINE-" + string;
        }

        return string1.equals(string2);
    }

    public static boolean equalsEnchants(Map<Enchantment, Integer> item1, Map<Enchantment, Integer> item2) {
        if (item1 == null && item2 == null) return true;
        if (item1 != null && item2 == null) return false;
        if (item2 != null && item1 == null) return false;
        if (item1.size() != item2.size()) return false;
        for (Enchantment e : item1.keySet()) {
            if (!item2.containsKey(e)) return false;
            if (item1.get(e) != item2.get(e)) return false;
        }
        return true;
    }

    public static boolean isItemEqual(ItemStack item, ItemStack SFitem) {
        return isItemEqual(item, SFitem, true);
    }

    public static boolean isItemEqual(ItemStack item, ItemStack SFitem, boolean checkEnchants) {
        if (item == null) return SFitem == null;
        if (SFitem == null) return false;
        if (item.getType() == SFitem.getType()) {//&& item.getAmount() >= SFitem.getAmount()
            if (isWeapon(item) || isArmor(item)) {
                if (item.getData().getData() != SFitem.getData().getData()) {
                    if (!(SFitem.getDurability() == item.getData().getData() && SFitem.getData().getData() == item.getDurability()))
                        return false;
                }
            }
            if (checkEnchants) {
                if (!equalsEnchants(item.getEnchantments(), SFitem.getEnchantments())) return false;
            }
            if (item.hasItemMeta() && SFitem.hasItemMeta()) {
                ItemMeta a = item.getItemMeta();
                ItemMeta b = SFitem.getItemMeta();
                if (a instanceof BannerMeta && b instanceof BannerMeta) {
                    if (!((BannerMeta) a).getPatterns().equals(((BannerMeta) b).getPatterns())) return false;

                    if (((BannerMeta) a).getBaseColor() == null && ((BannerMeta) b).getBaseColor() != null)
                        return false;
                    if (((BannerMeta) a).getBaseColor() != null && ((BannerMeta) b).getBaseColor() == null)
                        return false;
                    if (((BannerMeta) a).getBaseColor() != null && ((BannerMeta) b).getBaseColor() != null) {
                        if (!((BannerMeta) a).getBaseColor().equals(((BannerMeta) b).getBaseColor())) return false;
                    }
                }
                if (a instanceof EnchantmentStorageMeta && b instanceof EnchantmentStorageMeta) {
                    if (((EnchantmentStorageMeta) a).getEnchants() != null && ((EnchantmentStorageMeta) b).getEnchants() == null)
                        return false;
                    if (((EnchantmentStorageMeta) a).getEnchants() == null && ((EnchantmentStorageMeta) b).getEnchants() != null)
                        return false;
                    if (((EnchantmentStorageMeta) a).getEnchants() != null && ((EnchantmentStorageMeta) b).getEnchants() != null) {
                        if (!((EnchantmentStorageMeta) a).getEnchants().equals(((EnchantmentStorageMeta) b).getEnchants()))
                            return false;
                    }
                }
                if (a instanceof SpawnEggMeta && b instanceof SpawnEggMeta) {
                    if (((SpawnEggMeta) a).getSpawnedType() != null && ((SpawnEggMeta) b).getSpawnedType() == null)
                        return false;
                    if (((SpawnEggMeta) a).getSpawnedType() == null && ((SpawnEggMeta) b).getSpawnedType() != null)
                        return false;
                    if (((SpawnEggMeta) a).getSpawnedType() != null && ((SpawnEggMeta) b).getSpawnedType() != null) {
                        if (!((SpawnEggMeta) a).getSpawnedType().equals(((SpawnEggMeta) b).getSpawnedType()))
                            return false;
                    }
                }
                if (a instanceof SkullMeta && b instanceof SkullMeta) {
                    /*if (CustomSkull.getTexture(item) != null && CustomSkull.getTexture(SFitem) != null) {
                        if (!CustomSkull.getTexture(item).equals(CustomSkull.getTexture(SFitem))) return false;
                    }*/
                }
                if (a instanceof PotionMeta && b instanceof PotionMeta) {
                    if (!((PotionMeta) a).getCustomEffects().equals(((PotionMeta) b).getCustomEffects())) return false;
                    if (!((PotionMeta) a).getBasePotionData().equals(((PotionMeta) b).getBasePotionData()))
                        return false;
                    if (!((PotionMeta) a).getBasePotionData().getType().equals(((PotionMeta) b).getBasePotionData().getType()))
                        return false;
                    if (((PotionMeta) a).getColor() != null && ((PotionMeta) b).getColor() == null) return false;
                    if (((PotionMeta) a).getColor() == null && ((PotionMeta) b).getColor() != null) return false;
                    if (((PotionMeta) a).getColor() != null && ((PotionMeta) b).getColor() != null) {
                        if (!((PotionMeta) a).getColor().equals(((PotionMeta) b).getColor())) return false;
                    }
                }
                if (a instanceof BookMeta && b instanceof BookMeta) {
                    if (!((BookMeta) a).getAuthor().equals(((BookMeta) b).getAuthor())) return false;
                    if (!((BookMeta) a).getGeneration().equals(((BookMeta) b).getGeneration())) return false;
                    if (!((BookMeta) a).getPages().equals(((BookMeta) b).getPages())) return false;
                    if (!((BookMeta) a).getTitle().equals(((BookMeta) b).getTitle())) return false;
                }
                if (a instanceof LeatherArmorMeta && b instanceof LeatherArmorMeta) {
                    if (!((LeatherArmorMeta) a).getColor().equals(((LeatherArmorMeta) b).getColor())) return false;
                }
                if (a instanceof FireworkMeta && b instanceof FireworkMeta) {
                    if (!((FireworkMeta) a).getEffects().equals(((FireworkMeta) b).getEffects())) return false;
                    if (((FireworkMeta) a).getPower() != ((FireworkMeta) b).getPower()) return false;
                }
                if (a instanceof KnowledgeBookMeta && b instanceof KnowledgeBookMeta) {
                    //return false;
                }
                if (a instanceof EnchantmentStorageMeta && b instanceof EnchantmentStorageMeta) {
                    if (((EnchantmentStorageMeta) a).getStoredEnchants().size() != ((EnchantmentStorageMeta) b).getStoredEnchants().size())
                        return false;
                    Map<Enchantment, Integer> aMap = ((EnchantmentStorageMeta) a).getStoredEnchants();
                    Map<Enchantment, Integer> bMap = ((EnchantmentStorageMeta) b).getStoredEnchants();
                    for (Enchantment enchantment : aMap.keySet()) {
                        if (!bMap.containsKey(enchantment)) return false;
                        if ((bMap.get(enchantment) != aMap.get(enchantment))) return false;
                    }
                }
                if (item.getItemMeta().hasDisplayName() && SFitem.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().getDisplayName().equals(SFitem.getItemMeta().getDisplayName())) {
                        if (item.getItemMeta().hasLore() && !SFitem.getItemMeta().hasLore()) {
                            return false;
                        }
                        if (item.getItemMeta().hasLore() && SFitem.getItemMeta().hasLore()) {
                            return equalsLore(item.getItemMeta().getLore(), SFitem.getItemMeta().getLore());
                        } else return !item.getItemMeta().hasLore() && !SFitem.getItemMeta().hasLore();
                    } else return false;
                } else if (!item.getItemMeta().hasDisplayName() && !SFitem.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().hasLore() && !SFitem.getItemMeta().hasLore()) {
                        return false;
                    }
                    if (item.getItemMeta().hasLore() && SFitem.getItemMeta().hasLore()) {
                        return equalsLore(item.getItemMeta().getLore(), SFitem.getItemMeta().getLore());
                    } else return !item.getItemMeta().hasLore() && !SFitem.getItemMeta().hasLore();

                } else return false;
            } else return !item.hasItemMeta() && !SFitem.hasItemMeta();
        } else return false;
    }

    public static boolean isArmor(ItemStack mat) {
        return isArmor(mat.getType());
    }

    public static boolean isArmor(Material mat) {
        switch (mat) {
            case DIAMOND_CHESTPLATE:
                LATE:
                return true;
            case CHAINMAIL_CHESTPLATE:
                return true;
            case GOLDEN_CHESTPLATE:
                return true;
            case IRON_CHESTPLATE:
                return true;
            case LEATHER_CHESTPLATE:
                return true;
            case DIAMOND_HELMET:
                return true;
            case LEATHER_HELMET:
                return true;
            case IRON_HELMET:
                return true;
            case CHAINMAIL_HELMET:
                return true;
            case GOLDEN_HELMET:
                return true;
            case DIAMOND_LEGGINGS:
                return true;
            case CHAINMAIL_LEGGINGS:
                return true;
            case GOLDEN_LEGGINGS:
                return true;
            case IRON_LEGGINGS:
                return true;
            case LEATHER_LEGGINGS:
                return true;
            case DIAMOND_BOOTS:
                return true;
            case CHAINMAIL_BOOTS:
                return true;
            case GOLDEN_BOOTS:
                return true;
            case IRON_BOOTS:
                return true;
            case LEATHER_BOOTS:
                return true;
            case ELYTRA:
                return true;
        }
        return false;
    }

    public static boolean isWeapon(ItemStack mat) {
        return isWeapon(mat.getType());
    }

    public static boolean hasCustomName(ItemStack mat) {
        if (mat.hasItemMeta()) {
            if (mat.getItemMeta().hasDisplayName()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeapon(Material mat) {
        switch (mat) {
            case DIAMOND_SWORD:
                return true;
            case GOLDEN_SWORD:
                return true;
            case IRON_SWORD:
                return true;
            case STONE_SWORD:
                return true;
            case WOODEN_SWORD:
                return true;
            case DIAMOND_AXE:
                return true;
            case GOLDEN_AXE:
                return true;
            case IRON_AXE:
                return true;
            case STONE_AXE:
                return true;
            case WOODEN_AXE:
                return true;
            case SHIELD:
                return true;
        }
        return false;
    }

    public static boolean isSpawnEgg(ItemStack itemStack) {
        if (!Utilities.isEmpty(itemStack)) {
            if (itemStack.getType().toString().toUpperCase().contains("_SPAWN_EGG")) {
                return true;
            }
        }
        return false;
    }

    public static void fixSpawnerPlace(Block block, EntityType type) {
        CreatureSpawner cs = ((CreatureSpawner) block.getState());
        cs.setSpawnedType(type);
        cs.update(true, false);
    }

    public static boolean isTool(ItemStack mat) {
        return isTool(mat.getType());
    }

    public static boolean isTool(Material mat) {
        switch (mat) {
            case DIAMOND_PICKAXE:
                return true;
            case GOLDEN_PICKAXE:
                return true;
            case IRON_PICKAXE:
                return true;
            case STONE_PICKAXE:
                return true;
            case WOODEN_PICKAXE:
                return true;
            case DIAMOND_AXE:
                return true;
            case GOLDEN_AXE:
                return true;
            case IRON_AXE:
                return true;
            case STONE_AXE:
                return true;
            case WOODEN_AXE:
                return true;
            case DIAMOND_SHOVEL:
                return true;
            case STONE_SHOVEL:
                return true;
            case GOLDEN_SHOVEL:
                return true;
            case IRON_SHOVEL:
                return true;
            case WOODEN_SHOVEL:
                return true;
            case DIAMOND_HOE:
                return true;
            case GOLDEN_HOE:
                return true;
            case IRON_HOE:
                return true;
            case STONE_HOE:
                return true;
            case WOODEN_HOE:
                return true;
            case SHEARS:
                return true;
            case FLINT_AND_STEEL:
                return true;
            case FISHING_ROD:
                return true;
        }
        return false;
    }

    public static String getName(ItemStack toName) {
        return getName(toName, true);
    }

    public static String getName(ItemStack toName, boolean stripcolor) {
        String name = toName.getType().name();
        if (toName.hasItemMeta()) {
            if (toName.getItemMeta().hasDisplayName()) {
                String test = toName.getItemMeta().getDisplayName();
                if (stripcolor) test = ChatColor.stripColor(test);
                if (test.length() > 0) {
                    return test;
                }

            }
        }
        return name;
    }
}
