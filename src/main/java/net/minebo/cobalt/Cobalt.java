package net.minebo.cobalt;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.minebo.cobalt.service.ServiceManager;
import net.minebo.cobalt.service.bossbar.BossBarService;
import net.minebo.cobalt.service.bossbar.example.ExampleBossBarProvider;
import net.minebo.cobalt.service.command.CommandService;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.menu.MenuService;
import net.minebo.cobalt.service.nametag.NametagService;
import net.minebo.cobalt.service.nametag.example.ExampleNametagProvider;
import net.minebo.cobalt.service.npc.NpcService;
import net.minebo.cobalt.service.prompt.PromptService;
import net.minebo.cobalt.service.scheduler.SchedulerService;
import net.minebo.cobalt.service.scheduler.example.EnderpearlCooldown;
import net.minebo.cobalt.service.scheduler.example.SaleTimer;
import net.minebo.cobalt.service.scoreboard.ScoreboardService;
import net.minebo.cobalt.service.scoreboard.example.ExampleScoreboardProvider;
import net.minebo.cobalt.service.store.StoreService;
import net.minebo.cobalt.service.tablist.TabListService;
import net.minebo.cobalt.service.tablist.example.ExampleTabListProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class Cobalt extends JavaPlugin {

   @Getter public static Cobalt instance;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      loadPacketEvents();
      registerServices();
   }

   public void registerServices() {
      ServiceManager.register(this, new StoreService(), new CommandService(), new MenuService(), new PromptService(), new BossBarService(), new TabListService(), new ScoreboardService(), new NametagService(), new SchedulerService(), new HologramService(), new NpcService());

      if(this.getConfig().getBoolean("testing", false)) {
         CobaltAPI.getBossBarService().registerProvider(new ExampleBossBarProvider(), 0);
         CobaltAPI.getTabListService().registerProvider(new ExampleTabListProvider(), 0);
         CobaltAPI.getScoreboardService().registerProvider(new ExampleScoreboardProvider(), 0);
         CobaltAPI.getNametagService().registerProvider(new ExampleNametagProvider(), 0);
         CobaltAPI.getSchedulerService().register(new SaleTimer());
         CobaltAPI.getSchedulerService().register(new EnderpearlCooldown());
         CobaltAPI.getSchedulerService().startGlobalTimer(CobaltAPI.getSchedulerService().getTimer("sale"), 1, TimeUnit.HOURS);
      }
   }

   public void loadPacketEvents() {
      if (PacketEvents.getAPI() == null) {
         PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
         PacketEvents.getAPI().load();
         PacketEvents.getAPI().init();
      }
   }

}
