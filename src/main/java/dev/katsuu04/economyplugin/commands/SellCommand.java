package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public SellCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("shop.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("economyplugin.sell")) {
            player.sendMessage(plugin.getMessage("sell.no-permission"));
            return true;
        }

        // Obtenir l'item dans la main du joueur
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(plugin.getMessage("sell.no-item-in-hand"));
            return true;
        }

        Material material = itemInHand.getType();

        // Déterminer la quantité à vendre
        int amountToSell = 1;
        if (args.length > 0) {
            try {
                amountToSell = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessage("sell.invalid-amount"));
                return true;
            }
        }

        if (itemInHand.getAmount() < amountToSell) {
            player.sendMessage(plugin.getMessage("sell.not-enough-items").replace("%item%", material.name()));
            return true;
        }

        // Déterminer la catégorie
        String category = args.length > 1 ? args[1] : getDefaultCategory(material);

        // Logique de vente de l'item
        double price = plugin.getItemManager().getPrice(material);
        if (price <= 0) {
            player.sendMessage(plugin.getMessage("sell.invalid-amount"));
            return true;
        }

        // Mettre à jour la quantité disponible dans la boutique
        int currentAmount = plugin.getConfig().getInt("shop.items." + material.name() + ".amount", 0);
        plugin.getConfig().set("shop.items." + material.name() + ".amount", currentAmount + amountToSell);
        plugin.getConfig().set("shop.items." + material.name() + ".price", price);
        plugin.getConfig().set("shop.items." + material.name() + ".available", true);
        plugin.getConfig().set("shop.items." + material.name() + ".category", category);
        plugin.getConfig().set("shop.items." + material.name() + ".seller", player.getName());
        plugin.getConfig().set("shop.items." + material.name() + ".sell-date", System.currentTimeMillis());
        plugin.saveConfig();

        // Retirer les items de l'inventaire du joueur
        itemInHand.setAmount(itemInHand.getAmount() - amountToSell);
        player.getInventory().setItemInMainHand(itemInHand);

        // Ajouter l'argent au joueur
        plugin.getEconomyManager().deposit(player.getUniqueId(), price * amountToSell);

        player.sendMessage(plugin.getMessage("sell.item-sold")
                .replace("%item%", material.name())
                .replace("%amount%", String.valueOf(amountToSell))
                .replace("%total_price%", String.valueOf(price * amountToSell))
                .replace("%category%", category));

        return true;
    }

    private String getDefaultCategory(Material material) {
        switch (material) {
            case DIRT:
            case GRASS_BLOCK:
            case STONE:
            case COBBLESTONE:
            case SAND:
                return "blocks";
            case REDSTONE:
            case COAL:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
                return "minerals";
            default:
                return "misc";
        }
    }
}
