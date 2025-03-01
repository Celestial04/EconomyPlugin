package dev.katsuu04.economyplugin.gui;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddItemGUI implements Listener {

    private final EconomyPlugin plugin;
    private final int itemsPerPage = 45;
    private final Map<Player, Integer> playerPages = new HashMap<>();
    private final Map<Player, Material> pendingPriceItems = new HashMap<>();

    public AddItemGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openAddItemMenu(Player player, int page) {
        playerPages.put(player, page);
        String title = plugin.formatMessage("additem.page-title", Map.of("page", String.valueOf(page + 1)));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, Material.values().length);

        for (int i = startIndex; i < endIndex; i++) {
            Material material = Material.values()[i];
            if (!material.isItem()) continue;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(material.name());
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }

        // Ajouter les boutons de navigation avec des identifiants uniques
        if (page > 0) {
            ItemStack previousPageButton = createButton(plugin.getMessage("additem.previous-page"), Material.ARROW, "previous_page");
            inventory.setItem(45, previousPageButton);
        }
        if (page < (int) Math.ceil((double) Material.values().length / itemsPerPage) - 1) {
            ItemStack nextPageButton = createButton(plugin.getMessage("additem.next-page"), Material.ARROW, "next_page");
            inventory.setItem(53, nextPageButton);
        }

        player.openInventory(inventory);
    }

    private ItemStack createButton(String displayName, Material material, String identifier) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Collections.singletonList(identifier)); // Utiliser la lore comme identifiant
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(plugin.getMessage("additem.page-title").replace("%page%", "")))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        int page = playerPages.getOrDefault(player, 0);

        if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            String identifier = clicked.getItemMeta().getLore().get(0);

            if ("previous_page".equals(identifier)) {
                openAddItemMenu(player, page - 1);
            } else if ("next_page".equals(identifier)) {
                openAddItemMenu(player, page + 1);
            }
        } else {
            Material material = clicked.getType();
            pendingPriceItems.put(player, material);
            player.sendMessage(plugin.getMessage("additem.enter-price").replace("%item%", material.name()));
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (pendingPriceItems.containsKey(player)) {
            event.setCancelled(true);

            Material material = pendingPriceItems.remove(player);
            try {
                double price = Double.parseDouble(event.getMessage());
                if (price < 0) {
                    player.sendMessage(plugin.getMessage("additem.positive-price"));
                    return;
                }

                plugin.getConfig().set("shop.items." + material.name() + ".price", price);
                plugin.getConfig().set("shop.items." + material.name() + ".available", true);
                plugin.saveConfig();

                player.sendMessage(plugin.formatMessage("additem.item-added", Map.of("item", material.name(), "price", String.valueOf(price))));
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessage("additem.invalid-price"));
            }
        }
    }
}
