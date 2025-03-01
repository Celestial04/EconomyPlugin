package dev.katsuu04.economyplugin.gui;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShopGUI implements Listener {

    private final EconomyPlugin plugin;
    private String currentCategory = "all";

    public ShopGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player) {
        openShop(player, currentCategory);
    }

    public void openShop(Player player, String category) {
        currentCategory = category;
        plugin.getLogger().info("Opening shop for category: " + category);

        Inventory shop = Bukkit.createInventory(null, 54, getShopTitle());

        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("shop.categories");
        int slotIndex = 0;
        if (categoriesSection != null) {
            for (String catKey : categoriesSection.getKeys(false)) {
                if (categoriesSection.getBoolean(catKey + ".enabled")) {
                    String displayName = categoriesSection.getString(catKey + ".display-name");
                    shop.setItem(slotIndex, createButton("§a" + displayName, Material.PAPER, "category:" + catKey));
                    slotIndex++;
                }
            }
        }

        int itemIndex = 9;
        for (String materialName : plugin.getConfig().getConfigurationSection("shop.items").getKeys(false)) {
            Material material = Material.valueOf(materialName);
            if (plugin.getConfig().getBoolean("shop.items." + materialName + ".available")) {
                String itemCategory = plugin.getConfig().getString("shop.items." + materialName + ".category");
                if (itemCategory == null) {
                    plugin.getLogger().warning(plugin.getMessage("shop.no-category").replace("%item%", materialName));
                    continue;
                }
                if (category.equalsIgnoreCase(itemCategory)) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        double price = plugin.getItemManager().getPrice(material);
                        int amount = plugin.getConfig().getInt("shop.items." + materialName + ".amount", 0);
                        String seller = plugin.getConfig().getString("shop.items." + materialName + ".seller");
                        long sellDate = plugin.getConfig().getLong("shop.items." + materialName + ".sell-date");
                        Date date = new Date(sellDate);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        meta.setDisplayName(plugin.getMessage("shop.item-name").replace("%item%", material.name()));
                        meta.setLore(List.of(
                                plugin.getMessage("shop.item-price").replace("%price%", String.valueOf(price)),
                                plugin.getMessage("shop.item-click"),
                                plugin.getMessage("general.item-quantity").replace("%amount%", String.valueOf(amount)),
                                plugin.getMessage("general.item-seller").replace("%seller%", seller),
                                plugin.getMessage("general.item-sell-date").replace("%date%", dateFormat.format(date))
                        ));
                        item.setItemMeta(meta);
                    }
                    shop.setItem(itemIndex, item);
                    itemIndex++;
                }
            }
        }

        player.openInventory(shop);
        player.sendMessage(plugin.getMessage("shop.opened"));
    }

    private String getShopTitle() {
        boolean hasGlobalDiscount = isGlobalDiscountApplied();
        String baseTitle = plugin.getMessage("shop.title");
        return hasGlobalDiscount
                ? plugin.getMessage("shop.title-with-discount").replace("%title%", baseTitle)
                : baseTitle;
    }

    private boolean isGlobalDiscountApplied() {
        return plugin.getConfig().getBoolean("shop.global-discount-applied", false);
    }

    private ItemStack createButton(String displayName, Material material, String identifier) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(List.of(identifier));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(getShopTitle())) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) {
            return;
        }

        String identifier = clicked.getItemMeta().getLore().get(0);

        if (identifier.startsWith("category:")) {
            String category = identifier.split(":")[1];
            updateShopInventory(player, category);
        } else {
            Material material = clicked.getType();
            double price = plugin.getItemManager().getPrice(material);
            int amount = plugin.getConfig().getInt("shop.items." + material.name() + ".amount", 0);

            if (amount <= 0) {
                player.sendMessage(plugin.getMessage("shop.insufficient-funds"));
                return;
            }

            if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < price) {
                player.sendMessage(plugin.getMessage("shop.insufficient-funds"));
                return;
            }

            if (plugin.getEconomyManager().withdraw(player.getUniqueId(), price)) {
                player.getInventory().addItem(new ItemStack(material));
                player.sendMessage(plugin.getMessage("shop.item-purchased")
                        .replace("%item%", material.name())
                        .replace("%price%", String.valueOf(price)));

                plugin.getConfig().set("shop.items." + material.name() + ".amount", amount - 1);
                plugin.saveConfig();

                if (amount - 1 == 0) {
                    plugin.getConfig().set("shop.items." + material.name(), null);
                    plugin.saveConfig();
                }

                updateShopInventory(player, currentCategory);
            } else {
                player.sendMessage(plugin.getMessage("shop.withdraw-error"));
            }
        }
    }

    private void updateShopInventory(Player player, String category) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        inventory.clear();

        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("shop.categories");
        int slotIndex = 0;
        if (categoriesSection != null) {
            for (String catKey : categoriesSection.getKeys(false)) {
                if (categoriesSection.getBoolean(catKey + ".enabled")) {
                    String displayName = categoriesSection.getString(catKey + ".display-name");
                    inventory.setItem(slotIndex, createButton("§a" + displayName, Material.PAPER, "category:" + catKey));
                    slotIndex++;
                }
            }
        }

        int itemIndex = 9;
        for (String materialName : plugin.getConfig().getConfigurationSection("shop.items").getKeys(false)) {
            Material material = Material.valueOf(materialName);
            if (plugin.getConfig().getBoolean("shop.items." + materialName + ".available")) {
                String itemCategory = plugin.getConfig().getString("shop.items." + materialName + ".category");
                if (itemCategory == null) {
                    plugin.getLogger().warning(plugin.getMessage("shop.no-category").replace("%item%", materialName));
                    continue;
                }
                if (category.equalsIgnoreCase(itemCategory)) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        double price = plugin.getItemManager().getPrice(material);
                        int amount = plugin.getConfig().getInt("shop.items." + materialName + ".amount", 0);
                        String seller = plugin.getConfig().getString("shop.items." + materialName + ".seller");
                        long sellDate = plugin.getConfig().getLong("shop.items." + materialName + ".sell-date");
                        Date date = new Date(sellDate);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        meta.setDisplayName(plugin.getMessage("shop.item-name").replace("%item%", material.name()));
                        meta.setLore(List.of(
                                plugin.getMessage("shop.item-price").replace("%price%", String.valueOf(price)),
                                plugin.getMessage("shop.item-click"),
                                plugin.getMessage("general.item-quantity").replace("%amount%", String.valueOf(amount)),
                                plugin.getMessage("general.item-seller").replace("%seller%", seller),
                                plugin.getMessage("general.item-sell-date").replace("%date%", dateFormat.format(date))
                        ));
                        item.setItemMeta(meta);
                    }
                    inventory.setItem(itemIndex, item);
                    itemIndex++;
                }
            }
        }

        player.updateInventory();
    }
}