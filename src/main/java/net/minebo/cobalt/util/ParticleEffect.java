package net.minebo.cobalt.util;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public enum ParticleEffect {

    POOF(Particle.POOF),
    EXPLOSION(Particle.EXPLOSION),
    EXPLOSION_EMITTER(Particle.EXPLOSION_EMITTER),
    FIREWORK(Particle.FIREWORK),
    BUBBLE(Particle.BUBBLE),
    SPLASH(Particle.SPLASH),
    FISHING(Particle.FISHING),
    UNDERWATER(Particle.UNDERWATER),
    CRIT(Particle.CRIT),
    ENCHANTED_HIT(Particle.ENCHANTED_HIT),
    SMOKE(Particle.SMOKE),
    LARGE_SMOKE(Particle.LARGE_SMOKE),
    EFFECT(Particle.EFFECT),
    INSTANT_EFFECT(Particle.INSTANT_EFFECT),
    ENTITY_EFFECT(Particle.ENTITY_EFFECT, Color.class),
    WITCH(Particle.WITCH),
    DRIPPING_WATER(Particle.DRIPPING_WATER),
    DRIPPING_LAVA(Particle.DRIPPING_LAVA),
    ANGRY_VILLAGER(Particle.ANGRY_VILLAGER),
    HAPPY_VILLAGER(Particle.HAPPY_VILLAGER),
    MYCELIUM(Particle.MYCELIUM),
    NOTE(Particle.NOTE),
    PORTAL(Particle.PORTAL),
    ENCHANT(Particle.ENCHANT),
    FLAME(Particle.FLAME),
    LAVA(Particle.LAVA),
    CLOUD(Particle.CLOUD),
    DUST(Particle.DUST, DustOptions.class),
    ITEM_SNOWBALL(Particle.ITEM_SNOWBALL),
    ITEM_SLIME(Particle.ITEM_SLIME),
    HEART(Particle.HEART),
    ITEM(Particle.ITEM, ItemStack.class),
    BLOCK(Particle.BLOCK, BlockData.class),
    RAIN(Particle.RAIN),
    ELDER_GUARDIAN(Particle.ELDER_GUARDIAN),
    DRAGON_BREATH(Particle.DRAGON_BREATH),
    END_ROD(Particle.END_ROD),
    DAMAGE_INDICATOR(Particle.DAMAGE_INDICATOR),
    SWEEP_ATTACK(Particle.SWEEP_ATTACK),
    FALLING_DUST(Particle.FALLING_DUST, BlockData.class),
    TOTEM_OF_UNDYING(Particle.TOTEM_OF_UNDYING),
    SPIT(Particle.SPIT),
    SQUID_INK(Particle.SQUID_INK),
    BUBBLE_POP(Particle.BUBBLE_POP),
    CURRENT_DOWN(Particle.CURRENT_DOWN),
    BUBBLE_COLUMN_UP(Particle.BUBBLE_COLUMN_UP),
    NAUTILUS(Particle.NAUTILUS),
    DOLPHIN(Particle.DOLPHIN),
    SNEEZE(Particle.SNEEZE),
    CAMPFIRE_COSY_SMOKE(Particle.CAMPFIRE_COSY_SMOKE),
    CAMPFIRE_SIGNAL_SMOKE(Particle.CAMPFIRE_SIGNAL_SMOKE),
    COMPOSTER(Particle.COMPOSTER),
    FLASH(Particle.FLASH),
    FALLING_LAVA(Particle.FALLING_LAVA),
    LANDING_LAVA(Particle.LANDING_LAVA),
    FALLING_WATER(Particle.FALLING_WATER),
    DRIPPING_HONEY(Particle.DRIPPING_HONEY),
    FALLING_HONEY(Particle.FALLING_HONEY),
    LANDING_HONEY(Particle.LANDING_HONEY),
    FALLING_NECTAR(Particle.FALLING_NECTAR),
    SOUL_FIRE_FLAME(Particle.SOUL_FIRE_FLAME),
    ASH(Particle.ASH),
    CRIMSON_SPORE(Particle.CRIMSON_SPORE),
    WARPED_SPORE(Particle.WARPED_SPORE),
    SOUL(Particle.SOUL),
    DRIPPING_OBSIDIAN_TEAR(Particle.DRIPPING_OBSIDIAN_TEAR),
    FALLING_OBSIDIAN_TEAR(Particle.FALLING_OBSIDIAN_TEAR),
    LANDING_OBSIDIAN_TEAR(Particle.LANDING_OBSIDIAN_TEAR),
    REVERSE_PORTAL(Particle.REVERSE_PORTAL),
    WHITE_ASH(Particle.WHITE_ASH),
    DUST_COLOR_TRANSITION(Particle.DUST_COLOR_TRANSITION, DustTransition.class),
    VIBRATION(Particle.VIBRATION, org.bukkit.Vibration.class),
    FALLING_SPORE_BLOSSOM(Particle.FALLING_SPORE_BLOSSOM),
    SPORE_BLOSSOM_AIR(Particle.SPORE_BLOSSOM_AIR),
    SMALL_FLAME(Particle.SMALL_FLAME),
    SNOWFLAKE(Particle.SNOWFLAKE),
    DRIPPING_DRIPSTONE_LAVA(Particle.DRIPPING_DRIPSTONE_LAVA),
    FALLING_DRIPSTONE_LAVA(Particle.FALLING_DRIPSTONE_LAVA),
    DRIPPING_DRIPSTONE_WATER(Particle.DRIPPING_DRIPSTONE_WATER),
    FALLING_DRIPSTONE_WATER(Particle.FALLING_DRIPSTONE_WATER),
    GLOW_SQUID_INK(Particle.GLOW_SQUID_INK),
    GLOW(Particle.GLOW),
    WAX_ON(Particle.WAX_ON),
    WAX_OFF(Particle.WAX_OFF),
    ELECTRIC_SPARK(Particle.ELECTRIC_SPARK),
    SCRAPE(Particle.SCRAPE),
    SONIC_BOOM(Particle.SONIC_BOOM),
    SCULK_SOUL(Particle.SCULK_SOUL),
    SCULK_CHARGE(Particle.SCULK_CHARGE, Float.class),
    SCULK_CHARGE_POP(Particle.SCULK_CHARGE_POP),
    SHRIEK(Particle.SHRIEK, Integer.class),
    CHERRY_LEAVES(Particle.CHERRY_LEAVES),
    PALE_OAK_LEAVES(Particle.PALE_OAK_LEAVES),
    EGG_CRACK(Particle.EGG_CRACK),
    DUST_PLUME(Particle.DUST_PLUME),
    WHITE_SMOKE(Particle.WHITE_SMOKE),
    GUST(Particle.GUST),
    SMALL_GUST(Particle.SMALL_GUST),
    GUST_EMITTER_LARGE(Particle.GUST_EMITTER_LARGE),
    GUST_EMITTER_SMALL(Particle.GUST_EMITTER_SMALL),
    TRIAL_SPAWNER_DETECTION(Particle.TRIAL_SPAWNER_DETECTION),
    TRIAL_SPAWNER_DETECTION_OMINOUS(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS),
    VAULT_CONNECTION(Particle.VAULT_CONNECTION),
    INFESTED(Particle.INFESTED),
    ITEM_COBWEB(Particle.ITEM_COBWEB),
    DUST_PILLAR(Particle.DUST_PILLAR, BlockData.class),
    BLOCK_CRUMBLE(Particle.BLOCK_CRUMBLE, BlockData.class),
    TRAIL(Particle.TRAIL), // Trail requires special class (blockdata/traildata), only add if needed
    OMINOUS_SPAWNING(Particle.OMINOUS_SPAWNING),
    RAID_OMEN(Particle.RAID_OMEN),
    TRIAL_OMEN(Particle.TRIAL_OMEN),
    BLOCK_MARKER(Particle.BLOCK_MARKER, BlockData.class);

    private static final Map<String, ParticleEffect> NAME_MAP = new HashMap<>();
    private final Particle bukkitParticle;
    private final Class<?> dataType;

    static {
        for (ParticleEffect effect : values()) {
            NAME_MAP.put(effect.name().toLowerCase(), effect);
        }
    }

    ParticleEffect(Particle particle) {
        this.bukkitParticle = particle;
        this.dataType = null;
    }
    ParticleEffect(Particle particle, Class<?> dataType) {
        this.bukkitParticle = particle;
        this.dataType = dataType;
    }

    public String getName() {
        return name();
    }

    public Particle getBukkitParticle() {
        return bukkitParticle;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public static ParticleEffect fromName(String name) {
        return NAME_MAP.get(name.toLowerCase());
    }

    // Basic particle display
    public void display(Location location, int amount, double offsetX, double offsetY, double offsetZ, double speed, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, offsetX, offsetY, offsetZ, speed);
        }
    }
    public void display(Location location, int amount, double offsetX, double offsetY, double offsetZ, double speed, List<Player> players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, offsetX, offsetY, offsetZ, speed);
        }
    }

    // Color support for REDSTONE, ENTITY_EFFECT, etc
    public void displayColor(Location location, org.bukkit.Color color, int amount, Player...players) {
        for (Player player : players) {
            if (bukkitParticle == Particle.DUST) {
                DustOptions data = new DustOptions(color, 1.0F);
                player.spawnParticle(Particle.DUST, location, amount, 0, 0, 0, 1.0, data);
            } else if (bukkitParticle == Particle.ENTITY_EFFECT) {
                float r = color.getRed() / 255F;
                float g = color.getGreen() / 255F;
                float b = color.getBlue() / 255F;
                player.spawnParticle(Particle.ENTITY_EFFECT, location, amount, r, g, b, 1.0);
            }
        }
    }

    // Special block-support particles
    public void displayBlock(Location location, BlockData blockData, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, 0, 0, 0, 0, blockData);
        }
    }
    public void displayBlock(Location location, Material blockType, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, 0, 0, 0, 0, blockType.createBlockData());
        }
    }
    // Item particles
    public void displayItem(Location location, ItemStack itemStack, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, 0, 0, 0, 0, itemStack);
        }
    }
    public void displayItem(Location location, Material itemType, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, 0, 0, 0, 0, new ItemStack(itemType));
        }
    }

    // DustTransition support
    public void displayDustTransition(Location location, DustTransition transition, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, amount, 0, 0, 0, 1.0, transition);
        }
    }

    // Vibration support
    public void displayVibration(Location location, org.bukkit.Vibration vibration, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(Particle.VIBRATION, location, amount, 0, 0, 0, 1.0, vibration);
        }
    }

    // SCULK_CHARGE + SHRIEK support (float & int)
    public void displayData(Location location, Object data, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, 0, 0, 0, 1.0, data);
        }
    }

    // Directional particles
    public void displayDirectional(Location location, Vector direction, double speed, int amount, Player...players) {
        for (Player player : players) {
            player.spawnParticle(bukkitParticle, location, amount, direction.getX(), direction.getY(), direction.getZ(), speed);
        }
    }
}