package dev.katsuu04.economyplugin.gui;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ShopGUI implements Listener {

    private final EconomyPlugin plugin;

    public ShopGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 54, plugin.getMessage("shop-title"));

        for (String materialName : plugin.getConfig().getConfigurationSection("shop.items").getKeys(false)) {
            Material material = Material.valueOf(materialName);
            if (plugin.getItemManager().isAvailable(material)) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    double price = plugin.getItemManager().getPrice(material);
                    meta.setDisplayName(plugin.formatMessage("shop-item-name", Map.of(
                            "item", material.name()
                    )));
                    meta.setLore(java.util.Arrays.asList(
                            plugin.formatMessage("shop-item-price", Map.of("price", String.valueOf(price))),
                            plugin.getMessage("shop-item-click")
                    ));
                    item.setItemMeta(meta);
                }
                shop.addItem(item);
            }
        }

        player.openInventory(shop);
        player.sendMessage(plugin.getMessage("shop-opened"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(plugin.getMessage("shop-title"))) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        Material material = clicked.getType();
        double price = plugin.getItemManager().getPrice(material);

        if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < price) {
            player.sendMessage(plugin.getMessage("insufficient-funds"));
            return;
        }

        if (plugin.getEconomyManager().withdraw(player.getUniqueId(), price)) {
            player.getInventory().addItem(new ItemStack(material));
            player.sendMessage(plugin.formatMessage("item-purchased", Map.of(
                    "item", material.name(),
                    "price", String.valueOf(price)
            )));
        } else {
            player.sendMessage(plugin.getMessage("withdraw-error"));
        }
    }
}
