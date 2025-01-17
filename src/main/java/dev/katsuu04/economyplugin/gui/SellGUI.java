package dev.katsuu04.economyplugin.gui;

import dev.katsuu04.economyplugin.EconomyPlugin;
import dev.katsuu04.economyplugin.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SellGUI implements Listener {

    private final EconomyPlugin plugin;

    public SellGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openSellMenu(Player player) {
        Inventory sellMenu = Bukkit.createInventory(null, 54, plugin.getMessage("sell-menu-title"));
        player.openInventory(sellMenu);
        player.sendMessage(plugin.getMessage("sell-menu-opened"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(plugin.getMessage("sell-menu-title"))) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        Material material = clicked.getType();
        double price = plugin.getItemManager().getPrice(material);


        if (price <= 0) {
            player.sendMessage(plugin.getMessage("sell-invalid-price"));
            return;
        }

        int amountToSell = clicked.getAmount();


        if (!InventoryUtils.hasFreeSpace(player, material, amountToSell)) {
            player.sendMessage(plugin.formatMessage("sell-not-enough-items", Map.of(
                    "item", material.name()
            )));
            return;
        }


        InventoryUtils.removeItems(player, material, amountToSell);
        plugin.getEconomyManager().deposit(player.getUniqueId(), price * amountToSell);

        player.sendMessage(plugin.formatMessage("sell-success", Map.of(
                "amount", String.valueOf(amountToSell),
                "item", material.name(),
                "total", String.valueOf(price * amountToSell)
        )));


        if (!plugin.getItemManager().isAvailable(material)) {
            plugin.getConfig().set("shop.items." + material.name() + ".price", price);
            plugin.getConfig().set("shop.items." + material.name() + ".available", true);
            plugin.saveConfig();

            player.sendMessage(plugin.formatMessage("sell-added-to-shop", Map.of(
                    "item", material.name()
            )));
        }
    }
}
