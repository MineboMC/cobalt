package net.minebo.cobalt.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketEventsHandler {
   public static PacketEvents packetEvents;

   public PacketEventsHandler(JavaPlugin plugin) {
      if (PacketEvents.getAPI() == null) {
         PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
         PacketEvents.getAPI().load();
         PacketEvents.getAPI().init();
      }

   }
}
