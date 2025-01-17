package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import dev.katsuu04.economyplugin.gui.SellGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand implements CommandExecutor {

    private final SellGUI sellGUI;

    public SellCommand(EconomyPlugin plugin) {
        this.sellGUI = new SellGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent ouvrir le menu de vente !");
            return true;
        }

        Player player = (Player) sender;
        sellGUI.openSellMenu(player);
        return true;
    }
}
