package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import dev.katsuu04.economyplugin.gui.ShopGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ShopCommand implements CommandExecutor {

    private final EconomyPlugin plugin;
    private final ShopGUI shopGUI;

    public ShopCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.shopGUI = new ShopGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String onlyPlayersMessage = plugin.getMessagesConfig().getString("commands.shop.only-players", "Seuls les joueurs peuvent ouvrir la boutique !");
            sender.sendMessage(onlyPlayersMessage.replace("&", "§"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            listShopItems(player);
        } else {
            shopGUI.openShop(player);
        }
        return true;
    }

    private void listShopItems(Player player) {
        player.sendMessage("§aItems disponibles dans la boutique :");
        for (String materialName : plugin.getConfig().getConfigurationSection("shop.items").getKeys(false)) {
            Material material = Material.valueOf(materialName);
            if (plugin.getItemManager().isAvailable(material)) {
                double price = plugin.getItemManager().getPrice(material);
                player.sendMessage(plugin.formatMessage("shop-item-name", Map.of("item", material.name()))
                        + " §e- Prix : " + price);
            }
        }
    }
}
