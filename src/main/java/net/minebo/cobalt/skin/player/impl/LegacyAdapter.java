package net.minebo.cobalt.skin.player.impl;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import java.util.Iterator;

import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.skin.SkinAPI;
import net.minebo.cobalt.skin.player.IPlayerAdapter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class LegacyAdapter implements IPlayerAdapter {
   private final SkinAPI skinAPI;

   public CachedSkin getByPlayer(Player player) {
      WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
      Iterator<WrappedSignedProperty> iterator = profile.getProperties().get("textures").iterator();
      if (!iterator.hasNext()) {
         return SkinAPI.DEFAULT;
      } else {
         WrappedSignedProperty prop = iterator.next();
         return new CachedSkin(player.getName(), prop.getValue(), prop.getSignature());
      }
   }

   public void registerSkin(Player player) {
      CachedSkin skin = this.getByPlayer(player);
      this.skinAPI.registerSkin(player.getName(), skin);
   }

}
