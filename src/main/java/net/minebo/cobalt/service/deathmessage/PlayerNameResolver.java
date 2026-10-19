package net.minebo.cobalt.service.deathmessage;

import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Resolves the Component shown for a player's name in a death message.
 * Register a resolver with a higher weight than the default (0) via
 * {@link DeathMessageService#registerNameResolver(PlayerNameResolver, int)}
 * to substitute nicknames, ranks, or other metadata from another plugin.
 *
 * <p>Intentionally viewer-agnostic: death messages broadcast to everyone,
 * so a single resolved name is used for all viewers.
 */
public interface PlayerNameResolver {
    Component resolve(UUID playerId);
}