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

public class PriceAdjustmentGUI implements Listener {

    private final EconomyPlugin plugin;

    public PriceAdjustmentGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openPriceAdjustmentMenu(Player player, Material material) {
        String title = plugin.formatMessage("price-adjustment-title", Map.of("item", material.name()));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        double price = plugin.getItemManager().getPrice(material);


        inventory.setItem(10, createButton(plugin.getMessage("price-adjustment-minus-10"), Material.RED_STAINED_GLASS_PANE));
        inventory.setItem(11, createButton(plugin.getMessage("price-adjustment-minus-1"), Material.RED_STAINED_GLASS_PANE));
        inventory.setItem(12, createButton(plugin.getMessage("price-adjustment-minus-0.5"), Material.RED_STAINED_GLASS_PANE));

        inventory.setItem(14, createButton(plugin.getMessage("price-adjustment-plus-0.5"), Material.GREEN_STAINED_GLASS_PANE));
        inventory.setItem(15, createButton(plugin.getMessage("price-adjustment-plus-1"), Material.GREEN_STAINED_GLASS_PANE));
        inventory.setItem(16, createButton(plugin.getMessage("price-adjustment-plus-10"), Material.GREEN_STAINED_GLASS_PANE));

        inventory.setItem(13, createButton(plugin.formatMessage("price-adjustment-current-price", Map.of(
                "price", String.valueOf(price)
        )), material));
        inventory.setItem(22, createButton(plugin.getMessage("price-adjustment-validate"), Material.FEATHER));

        player.openInventory(inventory);
    }

    private ItemStack createButton(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(plugin.getMessage("price-adjustment-title-prefix"))) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String itemName = event.getView().getTitle().replace(plugin.getMessage("price-adjustment-title-prefix"), "");
        Material material = Material.valueOf(itemName);

        double price = plugin.getItemManager().getPrice(material);

        switch (clicked.getItemMeta().getDisplayName()) {
            case "§c-10" -> price -= 10;
            case "§c-1" -> price -= 1;
            case "§c-0.5" -> price -= 0.5;
            case "§a+0.5" -> price += 0.5;
            case "§a+1" -> price += 1;
            case "§a+10" -> price += 10;
            case "§aValider" -> {
                plugin.getConfig().set("shop.items." + material.name() + ".price", price);
                plugin.saveConfig();
                player.sendMessage(plugin.formatMessage("price-adjustment-new-price", Map.of(
                        "item", material.name(),
                        "price", String.valueOf(price)
                )));
                player.closeInventory();
                return;
            }
        }

        if (price < 0) price = 0;
        plugin.getConfig().set("shop.items." + material.name() + ".price", price);
        plugin.saveConfig();

        openPriceAdjustmentMenu(player, material);
    }
}
