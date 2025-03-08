package net.minebo.cobalt.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minebo.cobalt.scoreboard.construct.Board;
import net.minebo.cobalt.scoreboard.listener.ScoreboardListener;
import net.minebo.cobalt.scoreboard.provider.ScoreboardProvider;
import net.minebo.cobalt.scoreboard.thread.ScoreboardThread;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardHandler {
   public static ScoreboardProvider scoreboardProvider;
   public static Map<UUID, Board> boards;

   public ScoreboardHandler(ScoreboardProvider provider, JavaPlugin plugin) {
      scoreboardProvider = provider;
      boards = new HashMap();
      Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), plugin);
      ScoreboardThread.start();
   }
}
