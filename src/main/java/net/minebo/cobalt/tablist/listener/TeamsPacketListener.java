package net.minebo.cobalt.tablist.listener;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import java.util.EnumSet;

import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.tablist.TablistHandler;
import net.minebo.cobalt.tablist.util.GlitchFixEvent;
import net.minebo.cobalt.tablist.util.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@AllArgsConstructor
public class TeamsPacketListener extends PacketListenerAbstract {
   private final PacketEventsAPI packetEvents;

   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO || event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE || event.getPacketType() == PacketType.Play.Server.TEAMS) {
         ServerManager serverManager = this.packetEvents.getServerManager();
         boolean isClientNew = serverManager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3);
         Player player = event.getPlayer();
         TablistHandler tablistHandler = TablistHandler.instance;
         if (player != null && (!tablistHandler.isIgnore1_7() || !PacketUtils.isLegacyClient(player))) {
            if (isClientNew && event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
               WrapperPlayServerPlayerInfoUpdate infoUpdate = new WrapperPlayServerPlayerInfoUpdate(event);
               EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> action = infoUpdate.getActions();
               if (!action.contains(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER)) {
                  return;
               }

               for(WrapperPlayServerPlayerInfoUpdate.PlayerInfo info : infoUpdate.getEntries()) {
                  UserProfile userProfile = info.getGameProfile();
                  if (userProfile != null) {
                     this.preventGlitch(player, userProfile);
                  }
               }
            } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
               WrapperPlayServerPlayerInfo infoPacket = new WrapperPlayServerPlayerInfo(event);
               WrapperPlayServerPlayerInfo.Action action = infoPacket.getAction();
               if (action != WrapperPlayServerPlayerInfo.Action.ADD_PLAYER) {
                  return;
               }

               for(WrapperPlayServerPlayerInfo.PlayerData data : infoPacket.getPlayerDataList()) {
                  UserProfile userProfile = data.getUserProfile();
                  if (userProfile != null) {
                     this.preventGlitch(player, userProfile);
                  }
               }
            }

         }
      }
   }

   private void preventGlitch(Player player, UserProfile userProfile) {
      if (player != null) {
         Player online = Bukkit.getPlayer(userProfile.getUUID());
         if (online != null) {
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.getTeam("rtab");
            if (team == null) {
               team = scoreboard.registerNewTeam("rtab");
            }

            if (!team.hasEntry(online.getName())) {
               team.addEntry(online.getName());
            }

            GlitchFixEvent glitchFixEvent = new GlitchFixEvent(player);
            if (TablistHandler.instance.getPlugin().isEnabled()) {
               Bukkit.getPluginManager().callEvent(glitchFixEvent);
            }

         }
      }
   }

}
