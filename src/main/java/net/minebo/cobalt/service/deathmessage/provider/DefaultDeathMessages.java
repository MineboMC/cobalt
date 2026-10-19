package net.minebo.cobalt.service.deathmessage.provider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.service.deathmessage.DeathCauseCategory;
import net.minebo.cobalt.service.deathmessage.DeathContext;
import net.minebo.cobalt.service.deathmessage.DeathMessageProvider;
import net.minebo.cobalt.service.deathmessage.DeathMessageSettings;
import net.minebo.cobalt.service.deathmessage.PlayerNameResolver;
import net.minebo.cobalt.service.deathmessage.registry.DeathMessageProviderRegistry;

import java.util.List;
import java.util.UUID;

public final class DefaultDeathMessages {

    private DefaultDeathMessages() {}

    public static void registerAll(DeathMessageProviderRegistry registry) {
        registry.register(DeathCauseCategory.PVP, DefaultDeathMessages::pvp, 0);
        registry.register(DeathCauseCategory.PROJECTILE, DefaultDeathMessages::projectile, 0);
        registry.register(DeathCauseCategory.MOB, DefaultDeathMessages::mob, 0);
        registry.register(DeathCauseCategory.FALL, plain(" fell from a high place."), 0);
        registry.register(DeathCauseCategory.FIRE, plain(" went up in flames."), 0);
        registry.register(DeathCauseCategory.LAVA, plain(" tried to swim in lava."), 0);
        registry.register(DeathCauseCategory.DROWNING, plain(" drowned."), 0);
        registry.register(DeathCauseCategory.VOID, plain(" fell out of the world."), 0);
        registry.register(DeathCauseCategory.EXPLOSION, plain(" blew up."), 0);
        registry.register(DeathCauseCategory.SUFFOCATION, plain(" suffocated in a wall."), 0);
        registry.register(DeathCauseCategory.STARVATION, plain(" starved to death."), 0);
        registry.register(DeathCauseCategory.POISON, plain(" was poisoned to death."), 0);
        registry.register(DeathCauseCategory.MAGIC, plain(" was killed by magic."), 0);
        registry.register(DeathCauseCategory.LIGHTNING, plain(" was struck by lightning."), 0);
        registry.register(DeathCauseCategory.FALLING_BLOCK, plain(" was squashed by a falling block."), 0);
        registry.register(DeathCauseCategory.CRAMMING, plain(" was squished too much."), 0);
        registry.register(DeathCauseCategory.FREEZE, plain(" froze to death."), 0);
        registry.register(DeathCauseCategory.SONIC_BOOM, plain(" was obliterated by a sonically-charged shriek."), 0);
        registry.register(DeathCauseCategory.WITHER, plain(" withered away."), 0);
        registry.register(DeathCauseCategory.DRAGON_BREATH, plain(" was roasted in dragon's breath."), 0);
        registry.register(DeathCauseCategory.GENERIC, plain(" died."), 0);
    }

    /**
     * Resolves a player's name Component, defaulting to white only if the
     * resolver didn't already set a color — so a custom rank/nickname color
     * from another plugin's resolver is preserved rather than overwritten.
     */
    private static Component name(UUID playerId, PlayerNameResolver names) {
        Component resolved = names.resolve(playerId);
        return resolved.color() != null ? resolved : resolved.color(NamedTextColor.RED);
    }

    private static Component assistSuffix(DeathContext context, PlayerNameResolver names, DeathMessageSettings settings) {
        List<UUID> assists = context.assists();
        if (!settings.showAssists() || assists.isEmpty()) {
            return Component.empty();
        }
        Component joined = Component.empty();
        for (int i = 0; i < assists.size(); i++) {
            joined = joined.append(name(assists.get(i), names));
            if (i < assists.size() - 1) {
                joined = joined.append(Component.text(", ", NamedTextColor.GRAY));
            }
        }
        return Component.text(" (assisted by ", NamedTextColor.GRAY)
                .append(joined)
                .append(Component.text(")", NamedTextColor.GRAY));
    }

    private static Component pvp(DeathContext context, PlayerNameResolver names, DeathMessageSettings settings) {
        Component message = name(context.victim(), names)
                .append(Component.text(" was slain by ", NamedTextColor.YELLOW))
                .append(context.hasKiller() ? name(context.killer(), names) : Component.text("someone", NamedTextColor.WHITE));
        if (!settings.hideWeapons() && context.weapon() != null) {
            message = message.append(Component.text(" using ", NamedTextColor.YELLOW))
                    .append(Component.text(context.weapon(), NamedTextColor.RED));
        }
        return message.append(Component.text(".", NamedTextColor.YELLOW))
                .append(assistSuffix(context, names, settings));
    }

    private static Component projectile(DeathContext context, PlayerNameResolver names, DeathMessageSettings settings) {
        Component message = name(context.victim(), names)
                .append(Component.text(" was shot by ", NamedTextColor.YELLOW))
                .append(context.hasKiller() ? name(context.killer(), names) : Component.text("something", NamedTextColor.WHITE));
        if (!settings.hideWeapons() && context.weapon() != null) {
            message = message.append(Component.text(" using a ", NamedTextColor.YELLOW))
                    .append(Component.text(context.weapon(), NamedTextColor.RED));
        }
        return message.append(Component.text(".", NamedTextColor.YELLOW))
                .append(assistSuffix(context, names, settings));
    }

    private static Component mob(DeathContext context, PlayerNameResolver names, DeathMessageSettings settings) {
        String source = context.weapon() == null ? "a hostile mob" : context.weapon();
        Component message = name(context.victim(), names).append(Component.text(" was killed by ", NamedTextColor.YELLOW));
        message = context.hasKiller()
                ? message.append(name(context.killer(), names)).append(Component.text("'s " + source + ".", NamedTextColor.YELLOW))
                : message.append(Component.text(source + ".", NamedTextColor.YELLOW));
        return message.append(assistSuffix(context, names, settings));
    }

    private static DeathMessageProvider plain(String suffix) {
        return (context, names, settings) -> name(context.victim(), names)
                .append(Component.text(suffix, NamedTextColor.YELLOW))
                .append(assistSuffix(context, names, settings));
    }
}