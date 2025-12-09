package net.minebo.cobalt.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static Integer getFirstEmptySlot(Inventory inventory) {
        if (inventory == null) return null;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }

        return null;
    }

    public static Integer getFirstEmptySlot(Inventory inventory, Material... materials) {
        if (inventory == null) return null;

        boolean hasMaterials = materials != null && materials.length > 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null) {
                return i;
            }

            if (hasMaterials) {
                Material itemType = item.getType();
                for (Material m : materials) {
                    if (m == null) continue;
                    if (itemType == m) {
                        return i;
                    }
                }
            }
        }

        return null;
    }

}
