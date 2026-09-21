package net.minebo.cobalt.service.nametag.example;

import net.minebo.cobalt.service.nametag.Nametag;
import net.minebo.cobalt.service.nametag.NametagProvider;
import org.bukkit.entity.Player;

public final class ExampleNametagProvider implements NametagProvider {

    @Override
    public Nametag provide(Player viewer, Player target) {
        if (viewer.hasPermission("cobalt.staff") && target.hasPermission("cobalt.staff")) {
            return Nametag.of("&c[Staff] &f", "");
        }
        if (target.hasPermission("cobalt.staff")) {
            return Nametag.of("&c[Staff] &f", "");
        }
        return Nametag.of("&7", "");
    }
}