package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AddItemCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public AddItemCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("shop.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("economyplugin.additem")) {
            player.sendMessage(plugin.getMessage("additem.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /additem <item> <category> [price]");
            return true;
        }

        Material material = Material.matchMaterial(args[0].toUpperCase());
        if (material == null) {
            player.sendMessage("§cItem inconnu: " + args[0]);
            return true;
        }

        String category = args[1];
        if (!isValidCategory(category)) {
            player.sendMessage("§cCatégorie invalide: " + category);
            return true;
        }

        double price = args.length > 2 ? Double.parseDouble(args[2]) : 10.0; // Prix par défaut

        // Ajouter l'item à la boutique
        plugin.getConfig().set("shop.items." + material.name() + ".price", price);
        plugin.getConfig().set("shop.items." + material.name() + ".available", true);
        plugin.getConfig().set("shop.items." + material.name() + ".category", category);
        plugin.saveConfig();

        player.sendMessage("§aL'item " + material.name() + " a été ajouté à la boutique dans la catégorie " + category + " !");
        return true;
    }

    private boolean isValidCategory(String category) {
        List<String> validCategories = plugin.getConfig().getStringList("shop.categories");
        return validCategories.contains(category);
    }
}
