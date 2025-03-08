package net.minebo.cobalt.tablist.setup;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.tablist.TablistHandler;
import net.minebo.cobalt.tablist.adapter.TabAdapter;
import net.minebo.cobalt.tablist.util.PacketUtils;
import net.minebo.cobalt.tablist.util.Skin;
import net.minebo.cobalt.tablist.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabLayout {

   private static final Logger log = LogManager.getLogger(TabLayout.class);
   public static String[] TAB_NAMES = new String[80];
   private final Map entryMapping = new HashMap();
   private final int mod = 4;
   private final int maxEntries;
   private final Player player;
   private boolean isFirstJoin = true;
   private final Scoreboard scoreboard;
   private String header;
   private String footer;

   public TabLayout(Player player) {
      this.maxEntries = PacketUtils.isLegacyClient(player) ? 60 : 80;
      this.player = player;
      if (!TablistHandler.instance.isHook() && player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
         this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      } else {
         this.scoreboard = player.getScoreboard();
      }

      player.setScoreboard(this.scoreboard);
   }

   public void create() {
      PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
      ServerManager manager = packetEvents.getServerManager();
      if (manager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
         List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> dataList = new ArrayList();

         for(int index = 0; index < 80; ++index) {
            int x = index % this.mod;
            if (x < 3 || !PacketUtils.isLegacyClient(this.player)) {
               int y = index / this.mod;
               int i = y * this.mod + x;
               UserProfile gameProfile = this.generateProfile(i);
               TabEntryInfo info = new TabEntryInfo(gameProfile);
               this.entryMapping.put(i, info);
               dataList.add(new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(gameProfile, true, 0, GameMode.SURVIVAL, !PacketUtils.isLegacyClient(this.player) ? AdventureSerializer.fromLegacyFormat(this.getTeamAt(i)) : null, (RemoteChatSession)null));
            }
         }

         WrapperPlayServerPlayerInfoUpdate packetInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, dataList);
         WrapperPlayServerPlayerInfoUpdate list = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, dataList);
         WrapperPlayServerPlayerInfoUpdate gamemode = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE, dataList);
         this.sendPacket(packetInfo);
         if (!PacketUtils.isLegacyClient(this.player)) {
            WrapperPlayServerPlayerInfoUpdate display = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, dataList);
            this.sendPacket(display);
         }

         this.sendPacket(list);
         this.sendPacket(gamemode);
      } else {
         WrapperPlayServerPlayerInfo packetInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData[0]);
         List<WrapperPlayServerPlayerInfo.PlayerData> dataList = packetInfo.getPlayerDataList();

         for(int index = 0; index < 80; ++index) {
            int x = index % this.mod;
            if (x < 3 || !PacketUtils.isLegacyClient(this.player)) {
               int y = index / this.mod;
               int i = y * this.mod + x;
               UserProfile gameProfile = this.generateProfile(i);
               TabEntryInfo info = new TabEntryInfo(gameProfile);
               this.entryMapping.put(i, info);
               dataList.add(new WrapperPlayServerPlayerInfo.PlayerData(!PacketUtils.isLegacyClient(this.player) ? AdventureSerializer.fromLegacyFormat(this.getTeamAt(i)) : null, gameProfile, GameMode.SURVIVAL, 0));
            }
         }

         this.sendPacket(packetInfo);
      }

      Team bukkitTeam = this.scoreboard.getTeam("rtab");
      if (bukkitTeam == null) {
         bukkitTeam = this.scoreboard.registerNewTeam("rtab");
      }

      for(Player target : Bukkit.getOnlinePlayers()) {
         if (target != null && !bukkitTeam.hasEntry(target.getName())) {
            bukkitTeam.addEntry(target.getName());
         }
      }

      for(int index = 0; index < 80; ++index) {
         int x = index % this.mod;
         if (x < 3 || !PacketUtils.isLegacyClient(this.player)) {
            int y = index / this.mod;
            int i = y * this.mod + x;
            String displayName = this.getTeamAt(i);
            String team = "$" + displayName;
            Team scoreboardTeam = this.scoreboard.getTeam(team);
            if (scoreboardTeam == null) {
               scoreboardTeam = this.scoreboard.registerNewTeam(team);
               scoreboardTeam.addEntry(displayName);
            }
         }
      }

      for(Player target : Bukkit.getOnlinePlayers()) {
         Team team = target.getScoreboard().getTeam("rtab");
         if (team != null && !team.hasEntry(this.player.getName())) {
            team.addEntry(this.player.getName());
         }
      }

   }

   public void refresh() {
      TablistHandler tablistHandler = TablistHandler.instance;

      try {
         List<TabEntry> entries = tablistHandler.getAdapter().getLines(this.player);
         if (entries.isEmpty()) {
            for(int i = 0; i < 80; ++i) {
               this.update(i, "", 0, Skin.DEFAULT_SKIN);
            }

            return;
         }

         for(int i = 0; i < 80; ++i) {
            TabEntry entry = i < entries.size() ? (TabEntry)entries.get(i) : null;
            if (entry == null) {
               this.update(i, "", 0, Skin.DEFAULT_SKIN);
            } else {
               int x = entry.getX();
               int y = entry.getY();
               if (x < 3 || !PacketUtils.isLegacyClient(this.player)) {
                  int index = y * this.mod + x;

                  try {
                     this.update(index, entry.getText(), entry.getPing(), entry.getSkin());
                  } catch (Exception e) {
                     log.fatal("[{}] There was an error updating tablist for {}", tablistHandler.getPlugin().getName(), this.player.getName());
                     log.error(e);
                     e.printStackTrace();
                  }
               }
            }
         }
      } catch (NullPointerException e) {
         if (!tablistHandler.getPlugin().getName().equals("Bolt") || tablistHandler.isDebug()) {
            log.fatal("[{}] There was an error updating tablist for {}", tablistHandler.getPlugin().getName(), this.player.getName());
            log.error(e);
            e.printStackTrace();
         }
      } catch (Exception e) {
         log.fatal("[{}] There was an error updating tablist for {}", tablistHandler.getPlugin().getName(), this.player.getName());
         log.error(e);
         e.printStackTrace();
      }

      this.setHeaderAndFooter();
      if (this.player.getScoreboard() != this.scoreboard && !tablistHandler.isHook()) {
         this.player.setScoreboard(this.scoreboard);
      }

   }

   public void cleanup() {
      for(int index = 0; index < 80; ++index) {
         String displayName = this.getTeamAt(index);
         String team = "$" + displayName;
         Team bukkitTeam = this.player.getScoreboard().getTeam(team);
         if (bukkitTeam != null) {
            bukkitTeam.unregister();
         }

         TabEntryInfo entry = (TabEntryInfo)this.entryMapping.get(index);
         if (entry != null) {
            UserProfile userProfile = entry.getProfile();
            PacketWrapper<?> playerInfoRemove = new WrapperPlayServerPlayerInfoRemove(new UUID[]{userProfile.getUUID()});
            this.sendPacket(playerInfoRemove);
         }
      }

      if (!TablistHandler.instance.isHook()) {
         this.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }

   }

   public void setHeaderAndFooter() {
      if (!PacketUtils.isLegacyClient(this.player)) {
         TabAdapter tablistAdapter = TablistHandler.instance.getAdapter();
         if (tablistAdapter != null) {
            String header = StringUtils.color(tablistAdapter.getHeader(this.player));
            String footer = StringUtils.color(tablistAdapter.getFooter(this.player));
            if (!header.equals(this.header) || !footer.equals(this.footer)) {
               this.header = header;
               this.footer = footer;
               WrapperPlayServerPlayerListHeaderAndFooter headerAndFooter = new WrapperPlayServerPlayerListHeaderAndFooter(AdventureSerializer.fromLegacyFormat(header), AdventureSerializer.fromLegacyFormat(footer));
               this.sendPacket(headerAndFooter);
            }
         }
      }
   }

   public void update(int index, String text, int ping, CachedSkin skin) {
      text = StringUtils.color(text);
      String[] splitString = StringUtils.split(text);
      String prefix = splitString[0];
      String suffix = splitString[1];
      String displayName = this.getTeamAt(index);
      String team = "$" + displayName;
      if ((team.length() > 16 || displayName.length() > 16) && TablistHandler.instance.isDebug()) {
         log.info("[TablistAPI] Team Name or Display Name is longer than 16");
      }

      if ((prefix.length() > 16 || suffix.length() > 16) && TablistHandler.instance.isDebug()) {
         log.info("[TablistAPI] Prefix or Suffix is longer than 16");
      }

      TabEntryInfo entry = (TabEntryInfo)this.entryMapping.get(index);
      if (entry != null) {
         boolean changed = false;
         if (!prefix.equals(entry.getPrefix())) {
            entry.setPrefix(prefix);
            changed = true;
         }

         if (!suffix.equals(entry.getSuffix())) {
            entry.setSuffix(suffix);
            changed = true;
         }

         if (PacketUtils.isLegacyClient(this.player)) {
            Scoreboard scoreboard = this.player.getScoreboard();
            Team bukkitTeam = scoreboard.getTeam(team);
            boolean teamExists = bukkitTeam != null;
            if (bukkitTeam == null) {
               bukkitTeam = scoreboard.registerNewTeam(team);
               bukkitTeam.addEntry(displayName);
            }

            if (changed || !teamExists) {
               bukkitTeam.setPrefix(prefix);
               bukkitTeam.setSuffix(!suffix.isEmpty() ? suffix : "");
            }

            this.updatePing(entry, ping);
         } else {
            boolean updated = this.updateSkin(entry, skin, text);
            this.updatePing(entry, ping);
            if (!updated && (changed || this.isFirstJoin)) {
               this.updateDisplayName(entry, text.isEmpty() ? this.getTeamAt(index) : text);
               if (this.isFirstJoin) {
                  this.isFirstJoin = false;
               }
            }
         }

      }
   }

   private void updatePing(TabEntryInfo info, int ping) {
      PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
      ServerManager serverManager = packetEvents.getServerManager();
      boolean isClientNew = serverManager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3);
      int lastConnection = info.getPing();
      if (lastConnection != ping) {
         info.setPing(ping);
         UserProfile gameProfile = info.getProfile();
         PacketWrapper<?> playerInfo;
         if (isClientNew) {
            playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(gameProfile, true, ping, GameMode.SURVIVAL, (Component)null, (RemoteChatSession)null)});
         } else {
            playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY, new WrapperPlayServerPlayerInfo.PlayerData[]{new WrapperPlayServerPlayerInfo.PlayerData((Component)null, gameProfile, GameMode.SURVIVAL, ping)});
         }

         this.sendPacket(playerInfo);
      }
   }

   private boolean updateSkin(TabEntryInfo info, CachedSkin skin, String text) {
      PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
      ServerManager serverManager = packetEvents.getServerManager();
      boolean isServerNew = serverManager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3);
      if (skin == null) {
         skin = Skin.DEFAULT_SKIN;
      }

      CachedSkin lastSkin = info.getSkin();
      if (skin.equals(lastSkin)) {
         return false;
      } else {
         info.setSkin(skin);
         UserProfile userProfile = info.getProfile();
         TextureProperty textureProperty = new TextureProperty("textures", skin.getValue(), skin.getSignature());
         userProfile.setTextureProperties(Collections.singletonList(textureProperty));
         int ping = info.getPing();
         PacketWrapper<?> playerInfoRemove = null;
         if (isServerNew) {
            playerInfoRemove = new WrapperPlayServerPlayerInfoRemove(new UUID[]{userProfile.getUUID()});
         } else {
            playerInfoRemove = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData[]{new WrapperPlayServerPlayerInfo.PlayerData((Component)null, userProfile, GameMode.SURVIVAL, ping)});
         }

         if (isServerNew) {
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo data = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(userProfile, true, ping, GameMode.SURVIVAL, !PacketUtils.isLegacyClient(this.player) ? AdventureSerializer.fromLegacyFormat(text) : null, (RemoteChatSession)null);
            WrapperPlayServerPlayerInfoUpdate add = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{data});
            WrapperPlayServerPlayerInfoUpdate list = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{data});
            if (PacketUtils.isModernClient(this.player)) {
               this.sendPacket(playerInfoRemove);
            }

            this.sendPacket(add);
            this.sendPacket(list);
            if (!PacketUtils.isLegacyClient(this.player)) {
               WrapperPlayServerPlayerInfoUpdate display = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{data});
               this.sendPacket(display);
            }
         } else {
            WrapperPlayServerPlayerInfo.PlayerData data = new WrapperPlayServerPlayerInfo.PlayerData(!PacketUtils.isLegacyClient(this.player) ? AdventureSerializer.fromLegacyFormat(text) : null, userProfile, GameMode.SURVIVAL, ping);
            PacketWrapper<?> playerInfoAdd = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData[]{data});
            if (PacketUtils.isModernClient(this.player)) {
               this.sendPacket(playerInfoRemove);
            }

            this.sendPacket(playerInfoAdd);
         }

         return true;
      }
   }

   private void sendPacket(PacketWrapper packetWrapper) {
      if (this.player != null) {
         PacketUtils.sendPacket(this.player, packetWrapper);
      }
   }

   private UserProfile generateProfile(int index) {
      CachedSkin defaultSkin = Skin.DEFAULT_SKIN;
      UserProfile gameProfile = new UserProfile(UUID.randomUUID(), this.getTeamAt(index));
      TextureProperty textureProperty = new TextureProperty("textures", defaultSkin.getValue(), defaultSkin.getSignature());
      gameProfile.setTextureProperties(Collections.singletonList(textureProperty));
      return gameProfile;
   }

   private void updateDisplayName(TabEntryInfo entry, String text) {
      PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
      ServerManager manager = packetEvents.getServerManager();
      UserProfile profile = entry.getProfile();
      PacketWrapper<?> display;
      if (manager.getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
         WrapperPlayServerPlayerInfoUpdate.PlayerInfo data = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, true, 0, (GameMode)null, AdventureSerializer.fromLegacyFormat(text), (RemoteChatSession)null);
         display = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{data});
      } else {
         WrapperPlayServerPlayerInfo.PlayerData data = new WrapperPlayServerPlayerInfo.PlayerData(AdventureSerializer.fromLegacyFormat(text), profile, (GameMode)null, 0);
         display = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, new WrapperPlayServerPlayerInfo.PlayerData[]{data});
      }

      this.sendPacket(display);
   }

   public String getTeamAt(int index) {
      return TAB_NAMES[index];
   }

   static {
      for(int i = 0; i < TAB_NAMES.length; ++i) {
         int x = i % 4;
         int y = i / 4;
         String name = "§0§" + x + (y > 9 ? "§" + String.valueOf(y).toCharArray()[0] + "§" + String.valueOf(y).toCharArray()[1] : "§0§" + String.valueOf(y).toCharArray()[0]) + String.valueOf(ChatColor.RESET);
         TAB_NAMES[i] = name;
      }

   }
}
