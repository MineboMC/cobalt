package net.minebo.cobalt.service.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface ScoreboardProvider {

    String getTitle(Player player);

    /**
     * Top to bottom. Max 15. Return null/empty to hide the board.
     */
    List<String> getLines(Player player);
}