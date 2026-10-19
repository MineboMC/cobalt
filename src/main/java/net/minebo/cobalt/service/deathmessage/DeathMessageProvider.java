package net.minebo.cobalt.service.deathmessage;

import net.kyori.adventure.text.Component;

public interface DeathMessageProvider {
    Component build(DeathContext context, PlayerNameResolver names, DeathMessageSettings settings);
}