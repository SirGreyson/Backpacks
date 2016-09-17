package co.reasondev.prison;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class BackpackCmd implements CommandExecutor {

    private PrisonBackpacks plugin;

    public BackpackCmd(PrisonBackpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            return sender.isOp() || sender.hasPermission("backpacks.admin") ?
                    msg(sender, "&cInvalid args! Try &6/backpack reload|addsign") : msg(sender, "&cInvalid args! Try &6/backpack drop|togglesell");
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp() && !sender.hasPermission("backpacks.admin")) {
                return msg(sender, "&cYou do not have permission to use this command!");
            }
            plugin.reloadConfig();
            return msg(sender, "&aConfiguration reloaded!");
        }
        if (args[0].equalsIgnoreCase("addsign")) {
            if (!sender.isOp() && !sender.hasPermission("backpacks.admin")) {
                return msg(sender, "&cYou do not have permission to use this command!");
            }
            if (sender instanceof ConsoleCommandSender) {
                return msg(sender, "&cError! This command cannot be run from the Console!");
            }
            Player p = (Player) sender;
            if (args.length != 2) {
                return msg(sender, "&cInvalid args! Try /backpack addsign <backpack>");
            }
            Backpack backpack = plugin.getManager().getFromID(args[1]);
            if (backpack == null) {
                return msg(sender, "&cError! There is no Backpack with that ID!");
            }
            Block b = p.getTargetBlock((Set<Material>) null, 15);
            if (b == null || !(b.getState() instanceof Sign)) {
                return msg(sender, "&cError! You must be looking at a Sign to use this command!");
            }
            if (plugin.getManager().getFromSign(b.getLocation()) != null) {
                return msg(sender, "&cError! This sign is already assigned to a Backpack!");
            }
            plugin.getManager().addSign(backpack, b.getLocation());
            return msg(sender, "&aSuccessfully added Sign for Backpack with ID of &6" + backpack.getId());
        }
        if (args[0].equalsIgnoreCase("drop")) {
            if (sender instanceof ConsoleCommandSender) {
                return msg(sender, "&cError! This command cannot be run from the Console!");
            }
            Player p = (Player) sender;
            Backpack backpack = plugin.getManager().getFromItem(p.getItemInHand());
            if (backpack == null) {
                return msg(sender, "&cYou must be holding a Backpack to use this command!");
            }
            p.getWorld().dropItemNaturally(p.getLocation(), p.getItemInHand());
            p.setItemInHand(null);
            return msg(p, String.format(Config.Messages.BACKPACK_DROPPED.val(), backpack.getDisplayName()));
        }
        if (args[0].equalsIgnoreCase("togglesell")) {
            if (sender instanceof ConsoleCommandSender) {
                return msg(sender, "&cError! This command cannot be run from the Console!");
            }
            Player p = (Player) sender;
            Backpack backpack = plugin.getManager().getFromItem(p.getItemInHand());
            if (backpack == null) {
                return msg(sender, "&cYou must be holding a Backpack to use this command!");
            }
            boolean toggle = plugin.getManager().toggleAutoSell(p.getItemInHand());
            return msg(sender, "&7AutoSelling for this Backpack has been " + (toggle ? "&aENABLED" : "&cDISABLED"));
        }
        return sender.isOp() || sender.hasPermission("backpacks.admin") ?
                msg(sender, "&cInvalid args! Try &6/backpack reload|addsign") : msg(sender, "&cInvalid args! Try &6/backpack drop|togglesell");
    }

    private boolean msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.Messages.PREFIX.val() + " " + msg));
        return true;
    }
}
