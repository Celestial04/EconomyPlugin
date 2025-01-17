package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import dev.katsuu04.economyplugin.gui.ItemSelectionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdjustPricesCommand implements CommandExecutor {

    private final EconomyPlugin plugin;
    private final ItemSelectionGUI itemSelectionGUI;

    public AdjustPricesCommand(EconomyPlugin plugin, ItemSelectionGUI itemSelectionGUI) {
        this.plugin = plugin;
        this.itemSelectionGUI = itemSelectionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String onlyPlayersMessage = plugin.getMessagesConfig().getString("commands.adjustprices.only-players", "Seuls les joueurs peuvent exécuter cette commande !");
            sender.sendMessage(onlyPlayersMessage.replace("&", "§"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("economyplugin.adjustprices")) {
            String noPermissionMessage = plugin.getMessagesConfig().getString("commands.adjustprices.no-permission", "Vous n'avez pas la permission d'utiliser cette commande !");
            player.sendMessage(noPermissionMessage.replace("&", "§"));
            return true;
        }

        itemSelectionGUI.openItemSelectionMenu(player);
        return true;
    }
}
