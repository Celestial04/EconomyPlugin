package dev.katsuu04.economyplugin.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    /**
     * Vérifie si un joueur a un certain nombre d'items dans son inventaire.
     *
     * @param player   Le joueur à vérifier.
     * @param material Le matériau de l'item.
     * @param amount   Le nombre requis.
     * @return True si le joueur possède au moins ce nombre d'items, sinon false.
     */
    public static boolean hasFreeSpace(Player player, Material material, int amount) {
        int freeSpace = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                freeSpace += material.getMaxStackSize();
            } else if (item.getType() == material) {
                freeSpace += material.getMaxStackSize() - item.getAmount();
            }

            if (freeSpace >= amount) return true;
        }
        return false;
    }


    /**
     * Retire un certain nombre d'items du type spécifié de l'inventaire du joueur.
     *
     * @param player   Le joueur à qui retirer les items.
     * @param material Le matériau de l'item.
     * @param amount   Le nombre à retirer.
     */
    public static void removeItems(Player player, Material material, int amount) {
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    contents[i] = null;
                    if (amount == 0) break;
                }
            }
        }

        player.getInventory().setContents(contents);
    }
}
