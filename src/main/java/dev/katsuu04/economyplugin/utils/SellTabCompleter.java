package dev.katsuu04.economyplugin.utils;

import dev.katsuu04.economyplugin.EconomyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class SellTabCompleter implements TabCompleter {

    private final EconomyPlugin plugin;

    public SellTabCompleter(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("shop.categories");
            if (categoriesSection != null) {
                for (String catKey : categoriesSection.getKeys(false)) {
                    if (categoriesSection.getBoolean(catKey + ".enabled")) {
                        completions.add(categoriesSection.getString(catKey + ".display-name"));
                    }
                }
            }
        }

        return completions;
    }
}
