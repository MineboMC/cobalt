package net.minebo.cobalt.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemBuilder {

    public ItemStack itemStack;

    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material);
    }

    public ItemBuilder setName(String name) {
        itemStack.getItemMeta().setDisplayName(ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(name)));
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        itemStack.getItemMeta().setLore(List.of(lore));
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        itemStack.getItemMeta().addEnchant(enchantment, level, true);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

}
