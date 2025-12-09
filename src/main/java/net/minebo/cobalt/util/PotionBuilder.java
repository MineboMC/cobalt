package net.minebo.cobalt.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

/**
 * Fluent builder for potions. Mirrors the style of the provided ItemBuilder.
 *
 * Example usage:
 * new PotionBuilder()
 *     .setType("splash")
 *     .setName("&6Splash of Speed")
 *     .addEffect(PotionEffectType.SPEED, 2, 30) // level 2 for 30 seconds
 *     .build();
 */
public class PotionBuilder {

    public ItemStack itemStack;
    public ItemMeta itemMeta;
    public PotionMeta potionMeta;

    public PotionBuilder() {
        this(Material.POTION);
    }

    /**
     * Construct with a specific potion material (POTION, SPLASH_POTION, LINGERING_POTION).
     * If another material is supplied it will default to POTION.
     */
    public PotionBuilder(Material material) {
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION) {
            material = Material.POTION;
        }
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            potionMeta = (PotionMeta) itemMeta;
        }
    }

    /**
     * Set display name (color codes with & are translated).
     */
    public PotionBuilder setName(String name) {
        if (itemMeta == null) itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        return this;
    }

    /**
     * Set lore lines.
     */
    public PotionBuilder setLore(String... lore) {
        if (itemMeta == null) itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(List.of(lore));
        if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        return this;
    }

    /**
     * Set potion base type (NORMAL, SPLASH, LINGERING) using a string.
     * Accepts "normal", "splash", "lingering" (case-insensitive).
     */
    public PotionBuilder setType(BottleType type) {
        if (type == null) type = BottleType.DRINK;
        switch (type) {
            case BottleType.SPLASH:
                itemStack.setType(Material.SPLASH_POTION);
                break;
            case BottleType.LINGER:
                itemStack.setType(Material.LINGERING_POTION);
                break;
            default:
                itemStack.setType(Material.POTION);
                break;
        }
        // refresh metas after changing material
        itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            potionMeta = (PotionMeta) itemMeta;
        } else {
            potionMeta = null;
        }
        return this;
    }

    /**
     * Add a custom potion effect.
     * @param effect The PotionEffectType to add.
     * @param level  The potion level (1 = amplifier 0). If <= 0, it will be treated as 1.
     * @param timeSeconds Duration in seconds. Converted to ticks (seconds * 20).
     */
    public PotionBuilder addEffect(PotionEffectType effect, int level, int timeSeconds) {
        if (effect == null) return this;
        if (level <= 0) level = 1;
        if (timeSeconds < 0) timeSeconds = 0;
        if (potionMeta == null) {
            itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        }
        if (potionMeta == null) return this; // can't add effect if meta is not potion meta for some reason

        int amplifier = Math.max(0, level - 1);
        int ticks = timeSeconds * 20;
        PotionEffect pe = new PotionEffect(effect, ticks, amplifier, false, true, true);
        potionMeta.addCustomEffect(pe, true);
        return this;
    }

    /**
     * Set base PotionType (e.g. INSTANT_HEAL, NIGHT_VISION, etc.)
     */
    public PotionBuilder setBasePotionType(PotionType type) {
        if (type == null) return this;
        if (potionMeta == null) {
            itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        }
        if (potionMeta != null) {
            potionMeta.setBasePotionType(type);
        }
        return this;
    }

    /**
     * Set a custom color for the potion (optional).
     */
    public PotionBuilder setColor(Color color) {
        if (color == null) return this;
        if (potionMeta == null) {
            itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        }
        if (potionMeta != null) {
            potionMeta.setColor(color);
        }
        return this;
    }

    /**
     * Set the amount of the potion.
     */
    public PotionBuilder setSize(int size) {
        if (potionMeta == null) {
            itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        }
        if (potionMeta != null) {
            potionMeta.setMaxStackSize(64);
        }
        itemStack.setAmount(size);
        return this;
    }

    /**
     * Build the ItemStack (applies the meta and returns the item).
     */
    public ItemStack build() {
        if (potionMeta != null) {
            itemStack.setItemMeta(potionMeta);
        }

        return itemStack;
    }

}