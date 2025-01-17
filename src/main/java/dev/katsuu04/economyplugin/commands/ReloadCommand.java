package dev.katsuu04.economyplugin.commands;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final EconomyPlugin plugin;

    public ReloadCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economyplugin.reload")) {
            String noPermissionMessage = plugin.getMessagesConfig().getString("commands.reload.no-permission", "Vous n'avez pas la permission d'utiliser cette commande.");
            sender.sendMessage(noPermissionMessage.replace("&", "§"));
            return true;
        }

        plugin.reloadConfig();
        plugin.reloadMessages();

        String successMessage = plugin.getMessagesConfig().getString("commands.reload.success", "La configuration a été rechargée avec succès !");
        sender.sendMessage(successMessage.replace("&", "§"));
        return true;
    }
}
