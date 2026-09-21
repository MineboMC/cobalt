package net.minebo.cobalt.service.nametag;

import org.bukkit.entity.Player;

public interface NametagProvider {

    Nametag provide(Player viewer, Player target);
}