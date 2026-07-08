package net.minebo.cobalt.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    public ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder setSkullOwner(UUID uuid) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(offlinePlayer);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullTexture(String base64Texture) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "CustomHead");
            profile.setProperty(new ProfileProperty("textures", base64Texture));

            meta.setPlayerProfile(profile);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[ItemBuilder] Failed to set skull texture: " + e.getMessage());
        }

        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(owner);

        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ColorUtil.translateColors(name));
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