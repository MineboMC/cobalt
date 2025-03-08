package net.minebo.cobalt.skin.player;

import net.minebo.cobalt.skin.CachedSkin;
import org.bukkit.entity.Player;

public interface IPlayerAdapter {
   CachedSkin getByPlayer(Player var1);

   void registerSkin(Player var1);
}
