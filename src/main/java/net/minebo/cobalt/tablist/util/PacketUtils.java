package net.minebo.cobalt.tablist.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Generated;
import org.bukkit.entity.Player;

public final class PacketUtils {
   public static boolean isLegacyClient(Player player) {
      ClientVersion version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
      return version.isOlderThan(ClientVersion.V_1_8);
   }

   public static boolean isModernClient(Player player) {
      ClientVersion version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
      return version.isNewerThanOrEquals(ClientVersion.V_1_16);
   }

   public static boolean isBrokenClient(Player player) {
      ClientVersion version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
      return version.isNewerThanOrEquals(ClientVersion.V_1_18);
   }

   public static void sendPacket(Player target, PacketWrapper packetWrapper) {
      PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
      PlayerManager manager = packetEvents.getPlayerManager();
      manager.sendPacket(target, packetWrapper);
   }

}
