package co.reasondev.prison;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Backpack {

    private String id, displayName;
    private int size, cost;

    public Backpack(String id, String displayName, int size, int cost) {
        this.id = id;
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.size = size;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isItem(ItemStack i) {
        return i != null && i.getType() == Material.ENDER_CHEST && i.hasItemMeta() &&
                i.getItemMeta().hasDisplayName() && i.getItemMeta().hasLore() &&
                i.getItemMeta().getDisplayName().startsWith(displayName + BackpackManager.INDEX);

    }

    public int getSize() {
        return size;
    }

    public boolean isInventory(Inventory i) {
        return i.getSize() == size && i.getTitle().startsWith(displayName + BackpackManager.INDEX);
    }

    public int getCost() {
        return cost;
    }

    public static Backpack deserialize(ConfigurationSection c) {
        return new Backpack(c.getName(), c.getString("DISPLAY_NAME"), c.getInt("SIZE"), c.getInt("COST_TOKENS"));
    }
}
