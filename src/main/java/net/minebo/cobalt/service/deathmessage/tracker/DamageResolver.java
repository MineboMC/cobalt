package net.minebo.cobalt.service.deathmessage.tracker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minebo.cobalt.service.deathmessage.DamageRecord;
import net.minebo.cobalt.service.deathmessage.DeathCauseCategory;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

public final class DamageResolver {

    private DamageResolver() {}

    public static DamageRecord resolve(EntityDamageEvent event) {
        double amount = event.getFinalDamage();
        long now = System.currentTimeMillis();

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();

            if (damager instanceof Player player) {
                return new DamageRecord(player.getUniqueId(), amount, DeathCauseCategory.PVP, weaponName(player), now);
            }

            if (damager instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();
                UUID attacker = shooter instanceof Player player ? player.getUniqueId() : null;
                return new DamageRecord(attacker, amount, DeathCauseCategory.PROJECTILE, entityName(damager), now);
            }

            // A tamed pet's kill is credited to its owner, so "how much a player participated" accounts for it.
            if (damager instanceof Tameable tameable && tameable.isTamed()
                    && tameable.getOwner() instanceof Player owner) {
                return new DamageRecord(owner.getUniqueId(), amount, DeathCauseCategory.MOB, entityName(damager), now);
            }

            return new DamageRecord(null, amount, DeathCauseCategory.MOB, entityName(damager), now);
        }

        DeathCauseCategory category = DeathCauseCategory.fromEnvironmental(event.getCause());
        return new DamageRecord(null, amount, category, null, now);
    }

    private static String weaponName(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            return "their fists";
        }
        if (hand.getItemMeta() != null && hand.getItemMeta().hasDisplayName()) {
            return plain(hand.getItemMeta().displayName());
        }
        return WordUtils.capitalizeFully(hand.getType().name().replace('_', ' '));
    }

    private static String entityName(Entity entity) {
        Component customName = entity.customName();
        if (customName != null) {
            return plain(customName);
        }
        return WordUtils.capitalizeFully(entity.getType().name().replace('_', ' '));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}