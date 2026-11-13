package net.minebo.cobalt.service.actionbar.example;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.actionbar.ActionBarProvider;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ExampleActionBarProvider implements ActionBarProvider {

    @Override
    public Component getText(Player player) {
        Location location = player.getLocation();
        return Coloring.toComponent("<aqua>" + location.getWorld().getName() + " <dark_gray>• <gray>"
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }
}
