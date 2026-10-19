package net.minebo.cobalt.service.deathmessage;

import org.bukkit.event.entity.EntityDamageEvent;

public enum DeathCauseCategory {
    PVP,
    PROJECTILE,
    MOB,
    FALL,
    FIRE,
    LAVA,
    DROWNING,
    VOID,
    EXPLOSION,
    SUFFOCATION,
    STARVATION,
    POISON,
    MAGIC,
    LIGHTNING,
    FALLING_BLOCK,
    CRAMMING,
    FREEZE,
    SONIC_BOOM,
    WITHER,
    DRAGON_BREATH,
    GENERIC;

    public static DeathCauseCategory fromEnvironmental(EntityDamageEvent.DamageCause cause) {
        if (cause == null) {
            return GENERIC;
        }
        return switch (cause.name()) {
            case "FALL" -> FALL;
            case "FIRE", "FIRE_TICK" -> FIRE;
            case "LAVA" -> LAVA;
            case "DROWNING" -> DROWNING;
            case "VOID" -> VOID;
            case "BLOCK_EXPLOSION", "ENTITY_EXPLOSION" -> EXPLOSION;
            case "SUFFOCATION" -> SUFFOCATION;
            case "STARVATION" -> STARVATION;
            case "POISON" -> POISON;
            case "MAGIC", "POTION_EFFECT" -> MAGIC;
            case "LIGHTNING" -> LIGHTNING;
            case "FALLING_BLOCK" -> FALLING_BLOCK;
            case "CRAMMING" -> CRAMMING;
            case "FREEZE" -> FREEZE;
            case "SONIC_BOOM" -> SONIC_BOOM;
            case "WITHER" -> WITHER;
            case "DRAGON_BREATH" -> DRAGON_BREATH;
            default -> GENERIC;
        };
    }
}