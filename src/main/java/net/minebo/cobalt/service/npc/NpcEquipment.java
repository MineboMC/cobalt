package net.minebo.cobalt.service.npc;

import org.bukkit.inventory.ItemStack;

public final class NpcEquipment {

    private ItemStack helmet, chestplate, leggings, boots, hand, offHand;

    public ItemStack helmet() { return clone(helmet); }
    public ItemStack chestplate() { return clone(chestplate); }
    public ItemStack leggings() { return clone(leggings); }
    public ItemStack boots() { return clone(boots); }
    public ItemStack hand() { return clone(hand); }
    public ItemStack offHand() { return clone(offHand); }

    public void setHelmet(ItemStack item) { helmet = clone(item); }
    public void setChestplate(ItemStack item) { chestplate = clone(item); }
    public void setLeggings(ItemStack item) { leggings = clone(item); }
    public void setBoots(ItemStack item) { boots = clone(item); }
    public void setHand(ItemStack item) { hand = clone(item); }
    public void setOffHand(ItemStack item) { offHand = clone(item); }

    private static ItemStack clone(ItemStack item) {
        return item == null ? null : item.clone();
    }
}