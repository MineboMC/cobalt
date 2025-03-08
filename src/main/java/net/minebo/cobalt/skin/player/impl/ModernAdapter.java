package net.minebo.cobalt.skin.player.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.skin.SkinAPI;
import net.minebo.cobalt.skin.player.IPlayerAdapter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ModernAdapter implements IPlayerAdapter {
   private final SkinAPI skinAPI;

   public CachedSkin getByPlayer(Player player) {
      PlayerProfile profile = (PlayerProfile)player.getPlayerProfile();
      if (profile.hasTextures() && profile.isComplete()) {
         ProfileProperty profileProperty = null;

         for(ProfileProperty property : profile.getProperties()) {
            if (property.getName().equals("textures")) {
               profileProperty = property;
               break;
            }
         }

         return profileProperty == null ? SkinAPI.DEFAULT : new CachedSkin(profile.getName(), profileProperty.getValue(), profileProperty.getSignature());
      } else {
         return SkinAPI.DEFAULT;
      }
   }

   public void registerSkin(Player player) {
      CachedSkin skin = this.getByPlayer(player);
      this.skinAPI.registerSkin(player.getName(), skin);
   }

}
