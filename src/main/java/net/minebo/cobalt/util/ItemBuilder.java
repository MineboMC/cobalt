package net.minebo.cobalt.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    public ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    /**
     * Set skull to a player's head using UUID
     */
    public ItemBuilder setSkullOwner(UUID uuid) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(offlinePlayer);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Set skull to a player's head using username (not recommended, use UUID when possible)
     */
    public ItemBuilder setSkullOwner(String playerName) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(playerName);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Set custom texture using Base64 value (most common method)
     * Example texture: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv..."}}
     */
    public ItemBuilder setSkullTexture(String base64Texture) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            PlayerTextures skullProfile = profile.getTextures();
            URL url = new URL("http://textures.minecraft.net/texture/" + base64Texture); // Some plugins strip the full URL

            skullProfile.setSkin(url);
            meta.setPlayerProfile(profile);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().warning("[ItemBuilder] Invalid skull texture URL for Base64: " + base64Texture);
        }

        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(name)));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(List.of(lore));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchantment(org.bukkit.enchantments.Enchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSize(Integer size) {
        itemStack.setAmount(size);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDyeColor(org.bukkit.DyeColor dyeColor) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(dyeColor.getColor());
            itemStack.setItemMeta(leatherMeta);
        }
        return this;
    }

    public ItemBuilder setColor(org.bukkit.Color color) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
            itemStack.setItemMeta(leatherMeta);
        }
        return this;
    }

    public ItemBuilder setHexColor(String color) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(DyeUtil.fromHex(color));
            itemStack.setItemMeta(leatherMeta);
        }
        return this;
    }

    public ItemBuilder setPotionType(org.bukkit.potion.PotionType potionType) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(potionType);
            itemStack.setItemMeta(potionMeta);
        }
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}