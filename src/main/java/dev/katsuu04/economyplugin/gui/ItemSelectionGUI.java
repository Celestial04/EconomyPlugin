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

public class ItemSelectionGUI implements Listener {

    private final EconomyPlugin plugin;

    public ItemSelectionGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openItemSelectionMenu(Player player) {
        String title = plugin.getMessage("item-selection-title");
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        for (Material material : Material.values()) {
            if (!material.isItem()) continue;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.formatMessage("item-selection-item-name", Map.of(
                        "item", material.name()
                )));
                meta.setLore(java.util.Arrays.asList(
                        plugin.getMessage("item-selection-item-lore")
                ));
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(plugin.getMessage("item-selection-title"))) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        Material material = clicked.getType();

        new PriceAdjustmentGUI(plugin).openPriceAdjustmentMenu(player, material);
    }
}
