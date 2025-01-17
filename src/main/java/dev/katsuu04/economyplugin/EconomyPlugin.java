package dev.katsuu04.economyplugin;

import dev.katsuu04.economyplugin.commands.*;
import dev.katsuu04.economyplugin.gui.ItemSelectionGUI;
import dev.katsuu04.economyplugin.gui.PriceAdjustmentGUI;
import dev.katsuu04.economyplugin.gui.SellGUI;
import dev.katsuu04.economyplugin.gui.ShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyPlugin extends JavaPlugin {

    private final Map<UUID, Material> pendingPriceAdjustments = new HashMap<>();
    private EconomyManager economyManager;
    private ItemManager itemManager;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesFile();
        reloadMessages();

        // Initialiser les gestionnaires
        this.economyManager = new EconomyManager(this);
        this.itemManager = new ItemManager(this);
        ItemSelectionGUI itemSelectionGUI = new ItemSelectionGUI(this);
        PriceAdjustmentGUI priceAdjustmentGUI = new PriceAdjustmentGUI(this);

        // Enregistrer les commandes
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("setprice").setExecutor(new SetPriceCommand(this));
        getCommand("adjustprices").setExecutor(new AdjustPricesCommand(this, itemSelectionGUI));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("epReload").setExecutor(new ReloadCommand(this));


        getServer().getPluginManager().registerEvents(new PriceAdjustmentGUI(this), this);
        getServer().getPluginManager().registerEvents(new ShopGUI(this), this);
        getServer().getPluginManager().registerEvents(new SellGUI(this), this);
        getServer().getPluginManager().registerEvents(itemSelectionGUI, this);
        getServer().getPluginManager().registerEvents(priceAdjustmentGUI, this);

        getLogger().info("EconomyPlugin activé !");
    }

    public Map<UUID, Material> getPendingPriceAdjustments() {
        return pendingPriceAdjustments;
    }

    @Override
    public void onDisable() {
        getLogger().info("EconomyPlugin désactivé !");
    }

    private void loadMessagesFile() {
        saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString("messages." + path, "Message non configuré !");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void reloadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        reloadConfig();
    }

    public FileConfiguration getMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        return YamlConfiguration.loadConfiguration(messagesFile);
    }


    public String formatMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (pendingPriceAdjustments.containsKey(player.getUniqueId())) {
            event.setCancelled(true);

            Material material = pendingPriceAdjustments.remove(player.getUniqueId());
            try {
                double newPrice = Double.parseDouble(event.getMessage());
                if (newPrice < 0) {
                    player.sendMessage("§cLe prix doit être un nombre positif !");
                    return;
                }

                getConfig().set("shop.items." + material.name() + ".price", newPrice);
                saveConfig();
                player.sendMessage("§aLe prix de " + material.name() + " a été défini à §6" + newPrice + " §amonnaie !");
            } catch (NumberFormatException e) {
                player.sendMessage("§cVeuillez entrer un nombre valide !");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reloadmessages")) {
            reloadMessages();
            sender.sendMessage("§aMessages rechargés !");
            return true;
        }
        return false;
    }

}
