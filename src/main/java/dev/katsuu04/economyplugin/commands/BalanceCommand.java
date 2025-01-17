package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public BalanceCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("balance-not-player"));
            return true;
        }

        Player player = (Player) sender;
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        String balanceMessage = plugin.getMessage("balance-display").replace("{balance}", String.valueOf(balance));
        player.sendMessage(balanceMessage);
        return true;
    }
}
