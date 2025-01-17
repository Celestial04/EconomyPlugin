package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import dev.katsuu04.economyplugin.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        shopGUI.openShop(player);
        return true;
    }
}
