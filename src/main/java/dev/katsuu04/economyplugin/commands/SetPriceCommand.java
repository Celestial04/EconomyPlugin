package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetPriceCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public SetPriceCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economyplugin.setprice")) {
            sender.sendMessage(plugin.getMessage("setprice-no-permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getMessage("setprice-usage"));
            return true;
        }

        String materialName = args[0].toUpperCase();
        Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMessage("setprice-invalid-item").replace("{item}", materialName));
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price < 0) {
                sender.sendMessage(plugin.getMessage("setprice-negative-price"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("setprice-invalid-price"));
            return true;
        }

        plugin.getConfig().set("shop.items." + material.name() + ".price", price);
        plugin.saveConfig();

        sender.sendMessage(plugin.getMessage("setprice-success")
                .replace("{item}", material.name())
                .replace("{price}", String.valueOf(price)));
        return true;
    }
}
