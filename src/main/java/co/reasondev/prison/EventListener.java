package co.reasondev.prison;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static co.reasondev.prison.Config.Messages.*;

public class EventListener implements Listener {

    private PrisonBackpacks plugin;

    private Map<UUID, Long> cooldown = new HashMap<>();

    public EventListener(PrisonBackpacks plugin) {
        this.plugin = plugin;
    }

    private void msg(Player p, String message) {
        p.sendMessage(Config.Messages.PREFIX.val() + " " + ChatColor.translateAlternateColorCodes('&', message));
    }

    private void msg(Player p, Config.Messages message) {
        msg(p, message.val());
    }

    private void msg(Player p, Config.Messages message, Object... args) {
        msg(p, String.format(message.val(), args));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        if (!e.getLine(0).equalsIgnoreCase("[Backpack]")) {
            return;
        }
        if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("backpacks.admin")) {
            e.getBlock().breakNaturally();
            msg(e.getPlayer(), "&cYou do not have permission to make this sign!");
            return;
        }
        Backpack backpack = plugin.getManager().getFromID(e.getLine(1));
        if (backpack == null) {
            e.getBlock().breakNaturally();
            msg(e.getPlayer(), "&cThere is no backpack with that ID!");
            return;
        }
        plugin.getManager().addSign(backpack, e.getBlock().getLocation());
        e.setLine(0, Config.General.SIGN_HEADER.val());
        e.setLine(1, backpack.getDisplayName());
        e.setLine(2, ChatColor.GOLD + "" + backpack.getCost() + " Tokens");
        msg(e.getPlayer(), "&aSuccessfully created new Sign for Backpack with ID &e" + backpack.getId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) {
            return;
        }
        Backpack backpack = plugin.getManager().getFromSign(e.getBlock().getLocation());
        if (backpack == null) {
            return;
        }
        if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("backpacks.admin")) {
            e.setCancelled(true);
            msg(e.getPlayer(), "&cYou do not have Permission to break this Sign!");
        } else {
            plugin.getManager().removeSign(backpack, e.getBlock().getLocation());
            msg(e.getPlayer(), "&aSuccessfully removed Sign for Backpack with ID &6" + backpack.getId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(e.getClickedBlock().getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) e.getClickedBlock().getState();
        Backpack backpack = plugin.getManager().getFromSign(sign.getLocation());
        if (backpack == null) {
            return;
        }
        e.setCancelled(true);
        if (PrisonTokens.getBothTokens(e.getPlayer()) < backpack.getCost()) {
            msg(e.getPlayer(), Config.Messages.BACKPACK_NO_TOKENS, backpack.getDisplayName());
        } else {
            if (cooldown.containsKey(e.getPlayer().getUniqueId()) &&
                    System.currentTimeMillis() - cooldown.get(e.getPlayer().getUniqueId()) < 5000) {
                msg(e.getPlayer(), BACKPACK_COOLDOWN);
            } else if (!e.getPlayer().getInventory().addItem(plugin.getManager().getNewItem(backpack)).isEmpty()) {
                msg(e.getPlayer(), BACKPACK_FULL_INVENTORY);
            } else {
                PrisonTokens.takeBothTokens(e.getPlayer(), backpack.getCost());
                msg(e.getPlayer(), BACKPACK_PURCHASED, backpack.getDisplayName(), backpack.getCost());
                cooldown.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_CHEST) {
            return;
        }
        Backpack backpack = plugin.getManager().getFromItem(e.getItem());
        if (backpack != null) {
            e.setCancelled(true);
            Inventory i = plugin.getManager().getInventory(backpack, e.getItem());
            if (i != null) {
                e.getPlayer().openInventory(i);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        Backpack backpack = plugin.getManager().getFromItem(e.getItemDrop().getItemStack());
        if (backpack == null) {
            return;
        }
        e.setCancelled(true);
        msg(e.getPlayer(), DROP_CONFIRM);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        Backpack backpack = plugin.getManager().getFromInv(e.getInventory());
        if (backpack != null) {
            plugin.getManager().saveInventory(backpack, e.getInventory());
        }
    }
}
