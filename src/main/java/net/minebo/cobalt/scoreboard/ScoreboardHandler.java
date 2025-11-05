package net.minebo.cobalt.scoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minebo.cobalt.scoreboard.construct.Board;
import net.minebo.cobalt.scoreboard.listener.ScoreboardListener;
import net.minebo.cobalt.scoreboard.provider.ScoreboardProvider;
import net.minebo.cobalt.scoreboard.thread.ScoreboardThread;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardHandler {
    public static List<ScoreboardProvider> scoreboardProviders;
    public static Map<UUID, Board> boards;

    public ScoreboardHandler(List<ScoreboardProvider> providers, JavaPlugin plugin) {
       scoreboardProviders = providers;
       boards = new HashMap<>();
       Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), plugin);
       ScoreboardThread.start();
    }

    public static ScoreboardProvider getHighestPriorityProvider() {
        return scoreboardProviders.stream()
            .max(Comparator.comparingInt(provider -> provider.getPriority().getPriority()))
            .orElse(null);
    }

    public static void removeBoard(Player player) {
        Board board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public static String getCurrentScoreboardId(Player player) {
        Board board = boards.get(player.getUniqueId());
        return board != null ? board.getId() : null;
    }
}
