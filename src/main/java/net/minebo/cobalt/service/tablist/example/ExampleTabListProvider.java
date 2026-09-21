package net.minebo.cobalt.service.tablist.example;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.tablist.TabLayout;
import net.minebo.cobalt.service.tablist.TabListProvider;
import net.minebo.cobalt.service.tablist.TabSkin;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ExampleTabListProvider implements TabListProvider {

    @Override
    public TabLayout provide(Player player) {
        TabLayout layout = new TabLayout(3);
        Location loc = player.getLocation();

        // column 0
        layout.set(0, 0, "&6Player Info:");
        layout.set(0, 1, "&7Kills: 0");
        layout.set(0, 2, "&7Deaths: 0");
        layout.set(0, 4, "&6Your Location:");
        layout.set(0, 5, "&f" + loc.getBlockX() + ", " + loc.getBlockZ());

        // column 1 — viewer first, with their skin
        layout.set(1, 0, "&6Online:");
        int row = 1;
        layout.set(1, row++, Coloring.toComponent("&a" + player.getName()), TabSkin.of(player));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player) || row >= TabLayout.ROWS) {
                continue;
            }
            layout.set(1, row++, Coloring.toComponent("&f" + online.getName()), TabSkin.of(online));
        }

        // column 2
        layout.set(2, 0, "&6Players Online:");
        layout.set(2, 1, "&f" + Bukkit.getOnlinePlayers().size());
        layout.set(2, 3, "&6Ping:");
        layout.set(2, 4, "&f" + player.getPing() + "ms");

        return layout;
    }

    @Override
    public Component getHeader(Player player) {
        return Coloring.toComponent("<gradient:#00e5ff:#0077ff>Cobalt Example Tablist</gradient>");
    }

    @Override
    public Component getFooter(Player player) {
        return Coloring.toComponent("&7Ping: " + player.getPing() + "ms");
    }
}