package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public PayCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("pay-not-player"));
            return true;
        }

        Player senderPlayer = (Player) sender;

        if (args.length != 2) {
            senderPlayer.sendMessage(plugin.getMessage("pay-usage"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            senderPlayer.sendMessage(plugin.getMessage("pay-player-not-found"));
            return true;
        }

        if (targetPlayer.equals(senderPlayer)) {
            senderPlayer.sendMessage(plugin.getMessage("pay-self-transfer"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                senderPlayer.sendMessage(plugin.getMessage("pay-invalid-amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(plugin.getMessage("pay-invalid-amount"));
            return true;
        }

        double senderBalance = plugin.getEconomyManager().getBalance(senderPlayer.getUniqueId());

        if (senderBalance < amount) {
            senderPlayer.sendMessage(plugin.getMessage("pay-insufficient-balance"));
            return true;
        }

        plugin.getEconomyManager().withdraw(senderPlayer.getUniqueId(), amount);
        plugin.getEconomyManager().deposit(targetPlayer.getUniqueId(), amount);

        senderPlayer.sendMessage(plugin.getMessage("pay-success")
                .replace("{amount}", String.valueOf(amount))
                .replace("{player}", targetPlayer.getName()));
        targetPlayer.sendMessage(plugin.getMessage("pay-received")
                .replace("{amount}", String.valueOf(amount))
                .replace("{player}", senderPlayer.getName()));

        return true;
    }
}
