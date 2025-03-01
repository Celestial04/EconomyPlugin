package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class DiscountCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public DiscountCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economyplugin.discount")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage : /discount <percentage>");
            return true;
        }

        double discountPercentage;
        try {
            discountPercentage = Double.parseDouble(args[0]);
            if (discountPercentage < 0 || discountPercentage > 100) {
                sender.sendMessage("§cLe pourcentage de réduction doit être compris entre 0 et 100.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cVeuillez entrer un pourcentage valide.");
            return true;
        }

        ConfigurationSection shopItems = plugin.getConfig().getConfigurationSection("shop.items");
        boolean anyItemDiscounted = false;

        for (String itemKey : shopItems.getKeys(false)) {
            double currentPrice = shopItems.getDouble(itemKey + ".price");
            double newPrice = currentPrice * (1 - discountPercentage / 100);

            shopItems.set(itemKey + ".price", newPrice);
            anyItemDiscounted = true;
        }

        plugin.getConfig().set("shop.global-discount-applied", anyItemDiscounted);
        plugin.saveConfig();

        if (anyItemDiscounted) {
            sender.sendMessage("§aTous les prix du shop ont été réduits de " + discountPercentage + "%.");
        } else {
            sender.sendMessage("§cAucun item n'a été trouvé dans le shop pour appliquer la réduction.");
        }

        return true;
    }
}