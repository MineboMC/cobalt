package net.minebo.cobalt.tablist.setup;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.tablist.util.Skin;

@Getter @Setter
@AllArgsConstructor
public class TabEntryInfo {
   private final UserProfile profile;
   private int ping = 0;
   private CachedSkin skin;
   private String prefix;
   private String suffix;
   private WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo;

   public TabEntryInfo(UserProfile profile) {
      this.profile = profile;
   }

   public boolean equals(Object o) {
      if (!(o instanceof TabEntryInfo)) {
         return false;
      } else {
         return o != this ? false : ((TabEntryInfo)o).getProfile().equals(this.profile);
      }
   }

   public int hashCode() {
      return this.profile.getUUID().hashCode() + 645;
   }

}
