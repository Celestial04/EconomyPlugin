package dev.katsuu04.economyplugin;

import org.bukkit.Material;

public class ItemManager {

    private final EconomyPlugin plugin;

    public ItemManager(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public double getPrice(Material material) {
        double price = plugin.getConfig().getDouble("shop.items." + material.name() + ".price", -1.0);
        if (price < 0) {

            return 10.0;
        }
        return price;
    }

    public boolean isAvailable(Material material) {
        return plugin.getConfig().getBoolean("shop.items." + material.name() + ".available", false);
    }
}
