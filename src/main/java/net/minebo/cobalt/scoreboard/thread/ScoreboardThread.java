package net.minebo.cobalt.scoreboard.thread;

import java.util.ArrayList;
import net.minebo.cobalt.scoreboard.ScoreboardHandler;
import net.minebo.cobalt.scoreboard.provider.ScoreboardProvider;
import org.bukkit.ChatColor;

public class ScoreboardThread {
    public static void start() {
       Thread scoreboardThread = new Thread(() -> {
          while(true) {
             ScoreboardHandler.boards.values().forEach((board) -> {
                ScoreboardProvider provider = ScoreboardHandler.getHighestPriorityProvider();
                if (provider != null) {
                    board.updateTitle(ChatColor.translateAlternateColorCodes('&', provider.getTitle(board.getPlayer())));
                    ArrayList<String> scores = new ArrayList();

                    for(String line : provider.getLines(board.getPlayer())) {
                       scores.add(ChatColor.translateAlternateColorCodes('&', line));
                    }

                    board.updateLines(scores);
                }
             });

             try {
                Thread.sleep(20L);
             } catch (InterruptedException e) {
                e.printStackTrace();
             }
          }
       }, "Cobalt - Scoreboard Thread");
       scoreboardThread.setDaemon(true);
       scoreboardThread.start();
    }
}
