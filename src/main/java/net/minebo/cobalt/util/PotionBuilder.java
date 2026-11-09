package net.minebo.cobalt.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class PotionBuilder {

    public ItemStack itemStack;
    public ItemMeta itemMeta;
    public PotionMeta potionMeta;

    /**
     * Construct with a specific potion material (POTION, SPLASH_POTION, LINGERING_POTION).
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
        itemMeta.setDisplayName(Coloring.translateColors(name));
        if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        return this;
    }

    /**
     * Set lore lines.
     */
    public PotionBuilder setLore(String...lore) {
        if (itemMeta == null) itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(List.of(lore));
        if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        return this;
    }

    /**
     * Add a custom potion effect.
     * @param effect The PotionEffectType to add.
     * @param level  The potion level (1 = amplifier 0). Anything <= 0 is treated as 1.
     * @param timeSeconds Duration in seconds.
     */
    public PotionBuilder addEffect(PotionEffectType effect, int level, int timeSeconds) {
        if (effect == null) return this;
        if (level <= 0) level = 1;
        if (timeSeconds < 0) timeSeconds = 0;
        if (potionMeta == null) {
            itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) potionMeta = (PotionMeta) itemMeta;
        }
        if (potionMeta == null) return this;

        int amplifier = Math.max(0, level - 1);
        int ticks = timeSeconds * 20;
        PotionEffect pe = new PotionEffect(effect, ticks, amplifier, false, true, true);
        potionMeta.addCustomEffect(pe, true);
        return this;
    }

    /**
     * Set base PotionType (e.g. HEALING, NIGHT_VISION).
     */
    public PotionBuilder setPotionColor(PotionType type) {
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
     * Set a custom color for the potion.
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