package net.minebo.cobalt.tablist;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import net.minebo.cobalt.gson.Gson;
import net.minebo.cobalt.packetevents.PacketEventsHandler;
import net.minebo.cobalt.skin.SkinAPI;
import net.minebo.cobalt.tablist.adapter.TabAdapter;
import net.minebo.cobalt.tablist.adapter.impl.ExampleAdapter;
import net.minebo.cobalt.tablist.listener.TabListener;
import net.minebo.cobalt.tablist.listener.TeamsPacketListener;
import net.minebo.cobalt.tablist.setup.TabLayout;
import net.minebo.cobalt.tablist.thread.TablistThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

@AllArgsConstructor
@Getter
public class TablistHandler {

   public static final Logger log = LogManager.getLogger(TablistHandler.class);
   public static TablistHandler instance;
   public final Map<UUID, TabLayout> layoutMapping = new ConcurrentHashMap();
   public final JavaPlugin plugin;
   public SkinAPI skinAPI;
   public TabAdapter adapter;
   public TabListener listener;
   public TablistThread thread;
   public final boolean debug;
   public boolean hook;
   public boolean ignore1_7;
   public long ticks = 20L;

   public TablistHandler(JavaPlugin plugin) {
      instance = this;
      this.plugin = plugin;
      this.debug = false;
      if (PacketEventsHandler.packetEvents == null) {
         new PacketEventsHandler(plugin);
      }

      this.init(PacketEvents.getAPI());
   }

   public TablistHandler(JavaPlugin plugin, boolean debug) {
      instance = this;
      this.plugin = plugin;
      this.debug = debug;
      if (PacketEventsHandler.packetEvents == null) {
         new PacketEventsHandler(plugin);
      }

      this.init(PacketEvents.getAPI());
   }

   public void init(PacketEventsAPI packetEventsAPI) {
      this.adapter = new ExampleAdapter();
      this.listener = new TabListener(this);
      this.setupSkinCache(new SkinAPI(this.plugin, Gson.GSON));
      PacketEvents.getAPI().getEventManager().registerListener(new TeamsPacketListener(PacketEvents.getAPI()));
      Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
   }

   public void setupSkinCache(SkinAPI skinAPI) {
      this.skinAPI = skinAPI;
   }

   public void registerAdapter(TabAdapter tabAdapter, long ticks) {
      Preconditions.checkNotNull(this.skinAPI, "SkinAPI was not registered!");
      this.adapter = (tabAdapter == null ? new ExampleAdapter() : tabAdapter);
      if (ticks < 20L) {
         log.info("[{}] Provided refresh tick rate for Tablist is too low, reverting to 20 ticks!", this.plugin.getName());
         this.ticks = 20L;
      } else {
         this.ticks = ticks;
      }

      if (Bukkit.getMaxPlayers() < 60) {
         log.fatal("[{}] Max Players is below 60, this will cause issues for players on 1.7 and below!", this.plugin.getName());
      }

      this.thread = new TablistThread(this);
      this.thread.start();
   }

   public void unload() {
      if (this.listener != null) {
         HandlerList.unregisterAll(this.listener);
         this.listener = null;
      }

      for(Map.Entry entry : this.layoutMapping.entrySet()) {
         UUID uuid = (UUID)entry.getKey();
         ((TabLayout)entry.getValue()).cleanup();
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            Team team = player.getScoreboard().getTeam("rtab");
            if (team != null) {
               team.unregister();
            }

            this.layoutMapping.remove(uuid);
         }
      }

      this.thread.interrupt();
      this.thread = null;
   }

}
