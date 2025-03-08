package net.minebo.cobalt.skin.listener;

import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.skin.SkinAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class SkinListener implements Listener {
   private final SkinAPI api;

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      CachedSkin cachedSkin = this.api.getSkin(player.getName());
      if (cachedSkin == null) {
         this.api.registerPlayer(player);
      }

   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.api.clearPlayer(player);
   }

}
