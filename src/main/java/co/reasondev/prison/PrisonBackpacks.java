package co.reasondev.prison;

import org.bukkit.plugin.java.JavaPlugin;

public class PrisonBackpacks extends JavaPlugin {

    private BackpackManager manager;

    public void onEnable() {
        saveDefaultConfig();
        getManager().loadBackpacks();
        Config.setConfig(this.getConfig());
        getCommand("backpack").setExecutor(new BackpackCmd(this));
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new SellListener(this), this);
        getLogger().info("has been enabled");
    }

    public void onDisable() {
        getManager().saveSigns();
        getLogger().info("has been disabled");
    }

    public BackpackManager getManager() {
        if (manager == null) {
            manager = new BackpackManager(this);
        }
        return manager;
    }
}
