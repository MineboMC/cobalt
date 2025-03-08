package net.minebo.cobalt.tablist.listener;

import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.tablist.TablistHandler;
import net.minebo.cobalt.tablist.setup.TabLayout;
import net.minebo.cobalt.tablist.util.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.units.qual.A;

@AllArgsConstructor
public class TabListener implements Listener {
   private final TablistHandler instance;

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (!this.instance.isIgnore1_7() || !PacketUtils.isLegacyClient(player)) {
         TabLayout layout = new TabLayout(player);
         layout.create();
         layout.setHeaderAndFooter();
         this.instance.getLayoutMapping().put(player.getUniqueId(), layout);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      Team team = player.getScoreboard().getTeam("rtab");
      if (team != null && player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
         if (team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
         }

         team.unregister();
      }

      this.instance.getLayoutMapping().remove(player.getUniqueId());
   }

}
