package co.reasondev.prison;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static FileConfiguration c;

    public static void setConfig(FileConfiguration config) {
        c = config;
    }

    public enum General {

        SIGN_HEADER;

        public String val() {
            return ChatColor.translateAlternateColorCodes('&', c.getString(name()));
        }
    }

    public enum Messages {

        PREFIX, BACKPACK_PURCHASED, BACKPACK_NO_TOKENS, BACKPACK_FULL_INVENTORY, BACKPACK_COOLDOWN,
        BACKPACK_DROPPED, DROP_CONFIRM, BACKPACK_SOLD;

        public String val() {
            return ChatColor.translateAlternateColorCodes('&', c.getString("messages." + name()));
        }
    }
}
