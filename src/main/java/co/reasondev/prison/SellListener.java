package co.reasondev.prison;

import me.clip.autosell.SellHandler;
import me.clip.autosell.events.SellAllEvent;
import me.clip.autosell.events.SignSellEvent;
import me.clip.autosell.objects.SellResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SellListener implements Listener {

    private PrisonBackpacks plugin;

    public SellListener(PrisonBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignSign(SignSellEvent e) {
        for (ItemStack i : e.getPlayer().getInventory().getContents()) {
            Backpack backpack = plugin.getManager().getFromItem(i);
            if (backpack == null) {
                continue;
            }
            if (!isSellAllowed(i)) {
                continue;
            }
            Inventory inv = plugin.getManager().getInventory(backpack, i);
            SellResponse response = SellHandler.sellItems(e.getPlayer(),
                    Arrays.asList(inv.getContents()), SellHandler.getShop(e.getPlayer()));
            msg(e.getPlayer(), String.format(Config.Messages.BACKPACK_SOLD.val(), response.getItemsSold(), backpack.getDisplayName()));
            if (response.getItemsSold() == 0) {
                continue;
            }
            for (ItemStack item : response.getSoldItems()) {
                inv.remove(item);
                plugin.getManager().saveInventory(backpack, inv);
            }
        }
    }

    @EventHandler
    public void onSellAll(SellAllEvent e) {
        for (ItemStack i : e.getPlayer().getInventory().getContents()) {
            Backpack backpack = plugin.getManager().getFromItem(i);
            if (backpack == null) {
                continue;
            }
            if (!isSellAllowed(i)) {
                continue;
            }
            Inventory inv = plugin.getManager().getInventory(backpack, i);
            SellResponse response = SellHandler.sellItems(e.getPlayer(),
                    Arrays.asList(inv.getContents()), SellHandler.getShop(e.getPlayer()));
            msg(e.getPlayer(), String.format(Config.Messages.BACKPACK_SOLD.val(), response.getItemsSold(), backpack.getDisplayName()));
            if (response.getItemsSold() == 0) {
                continue;
            }
            for (ItemStack item : response.getSoldItems()) {
                inv.remove(item);
                plugin.getManager().saveInventory(backpack, inv);
            }
        }
    }

    private boolean isSellAllowed(ItemStack i) {
        return i.getItemMeta().hasLore() && i.getItemMeta().getLore().get(0).contains("TRUE");
    }

    private void msg(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.Messages.PREFIX.val() + " " + msg));
    }
}
