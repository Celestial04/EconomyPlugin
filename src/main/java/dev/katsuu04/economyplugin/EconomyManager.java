package dev.katsuu04.economyplugin;

import java.util.HashMap;
import java.util.UUID;

public class EconomyManager {

    private final EconomyPlugin plugin;
    private final HashMap<UUID, Double> balances = new HashMap<>();

    public EconomyManager(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public void deposit(UUID uuid, double amount) {
        balances.put(uuid, getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = getBalance(uuid);
        if (balance >= amount) {
            balances.put(uuid, balance - amount);
            return true;
        }
        return false;
    }
}
