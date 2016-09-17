package co.reasondev.prison;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BackpackManager {

    private PrisonBackpacks plugin;

    private FileConfiguration signC;
    private Map<String, List<Location>> signLocations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private Map<String, Backpack> backpacks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, File> directories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static final String INDEX = ChatColor.GRAY + " #";

    public BackpackManager(PrisonBackpacks plugin) {
        this.plugin = plugin;
    }

    private void loadSignConfig() {
        File file = new File(plugin.getDataFolder(), "signs.yml");
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }
        signC = YamlConfiguration.loadConfiguration(file);
        for (String id : backpacks.keySet()) {
            if (!signC.isList(id)) {
                signC.set(id, new ArrayList<Location>());
            }
            signLocations.put(id, (List<Location>) signC.getList(id));
        }
    }

    public void saveSigns() {
        for (String id : signLocations.keySet()) {
            signC.set(id, signLocations.get(id));
        }
        try {
            signC.save(new File(plugin.getDataFolder(), "signs.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Error! Could not save Sign configuration!");
            e.printStackTrace();
        }
    }

    public Backpack getFromSign(Location loc) {
        for (String id : signLocations.keySet()) {
            if (signLocations.get(id).contains(loc)) {
                return getFromID(id);
            }
        }
        return null;
    }

    public void addSign(Backpack backpack, Location sign) {
        signLocations.get(backpack.getId()).add(sign);
    }

    public void removeSign(Backpack backpack, Location sign) {
        signLocations.get(backpack.getId()).remove(sign);
    }

    public void loadBackpacks() {
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("backpacks");
        for (String id : c.getKeys(false)) {
            backpacks.put(id, Backpack.deserialize(c.getConfigurationSection(id)));
            loadDirectory(id);
        }
        loadSignConfig();
        plugin.getLogger().info("Successfully loaded " + backpacks.size() + " Backpacks from config!");
    }

    private void loadDirectory(String id) {
        File root = new File(plugin.getDataFolder() + File.separator + "backpacks");
        if (!root.exists()) {
            root.mkdir();
        }
        File dir = new File(root + File.separator + id);
        if (!dir.exists()) {
            dir.mkdir();
        }
        directories.put(id, dir);
    }

    public Backpack getFromID(String id) {
        return backpacks.get(id);
    }

    public Backpack getFromDisplay(String displayName) {
        for (Backpack backpack : backpacks.values()) {
            if (backpack.getDisplayName().equals(displayName)) {
                return backpack;
            }
        }
        return null;
    }

    public Backpack getFromItem(ItemStack i) {
        for (Backpack backpack : backpacks.values()) {
            if (backpack.isItem(i)) {
                return backpack;
            }
        }
        return null;
    }

    public Backpack getFromInv(Inventory i) {
        for (Backpack backpack : backpacks.values()) {
            if (backpack.isInventory(i)) {
                return backpack;
            }
        }
        return null;
    }

    private int nextIndex(Backpack backpack) {
        File dir = directories.get(backpack.getId());
        List<String> files = Arrays.asList(dir.list());
        for (int i = 0; i <= files.size(); i++) {
            if (!files.contains(i + ".yml")) {
                return i;
            }
        }
        return 0;
    }

    public ItemStack getNewItem(Backpack backpack) {
        int index = nextIndex(backpack);
        ItemStack i = new ItemStack(Material.ENDER_CHEST, 1);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(backpack.getDisplayName() + BackpackManager.INDEX + index);
        meta.setLore(Arrays.asList(ChatColor.GRAY + "AutoSell: " + ChatColor.RED + "FALSE"));
        i.setItemMeta(meta);
        this.getFile(backpack, index);
        return i;
    }

    public boolean getAutoSell(ItemStack i) {
        String value = ChatColor.stripColor(i.getItemMeta().getLore().get(0).split(" ")[1]);
        return Boolean.parseBoolean(value);
    }

    public boolean toggleAutoSell(ItemStack i) {
        boolean current = getAutoSell(i);
        ItemMeta meta = i.getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(0, ChatColor.GRAY + "AutoSell: " + (current ? ChatColor.RED + "FALSE" : ChatColor.GREEN + "TRUE"));
        meta.setLore(lore);
        i.setItemMeta(meta);
        return !current;
    }

    private File getFile(Backpack backpack, int index) {
        File dir = directories.get(backpack.getId());
        File file = new File(dir, index + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error! Could not load File for Backpack [" + backpack.getId() + "] with Index of " + index);
                e.printStackTrace();
            }
        }
        return file;
    }

    public Inventory getInventory(Backpack backpack, ItemStack i) {
        int index = Integer.parseInt(i.getItemMeta().getDisplayName().split(INDEX)[1]);
        FileConfiguration c = YamlConfiguration.loadConfiguration(getFile(backpack, index));
        Inventory output = Bukkit.createInventory(null, backpack.getSize(), backpack.getDisplayName() + INDEX + index);
        for (int slot = 0; slot < output.getSize(); slot++) {
            if (c.isItemStack(String.valueOf(slot))) {
                output.setItem(slot, c.getItemStack(String.valueOf(slot)));
            }
        }
        return output;
    }

    public void saveInventory(Backpack backpack, Inventory inventory) {
        int index = Integer.parseInt(inventory.getTitle().split(INDEX)[1]);
        File file = getFile(backpack, index);
        FileConfiguration c = YamlConfiguration.loadConfiguration(file);
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            c.set(String.valueOf(i), item);
        }
        try {
            c.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error! Could not save File for Backpack [" + backpack.getId() + "] with Index of " + index);
            e.printStackTrace();
        }
    }
}
