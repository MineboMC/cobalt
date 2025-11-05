package net.minebo.cobalt.scoreboard.listener;

import net.minebo.cobalt.scoreboard.ScoreboardHandler;
import net.minebo.cobalt.scoreboard.construct.Board;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      ScoreboardHandler.boards.put(event.getPlayer().getUniqueId(), new Board(event.getPlayer()));
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Board board = ScoreboardHandler.boards.remove(event.getPlayer().getUniqueId());
      if (board != null) {
         board.delete();
      }
   }
}
