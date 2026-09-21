package net.minebo.cobalt;

import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.ServiceManager;
import net.minebo.cobalt.service.bossbar.BossBarService;
import net.minebo.cobalt.service.command.CommandService;
import net.minebo.cobalt.service.prompt.PromptService;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.menu.MenuService;
import net.minebo.cobalt.service.nametag.NametagService;
import net.minebo.cobalt.service.npc.NpcService;
import net.minebo.cobalt.service.scheduler.SchedulerService;
import net.minebo.cobalt.service.scoreboard.ScoreboardService;
import net.minebo.cobalt.service.store.StoreService;
import net.minebo.cobalt.service.tablist.TabListService;

import java.util.List;

public class CobaltAPI {

    public static List<CService> getServices() {
        return (ServiceManager.getServices(Cobalt.getInstance()) == null ? List.of() : ServiceManager.getServices(Cobalt.getInstance()));
    }

    public static BossBarService getBossBarService() {
        return getServices().stream().filter(s -> s instanceof BossBarService).map(s -> (BossBarService) s).findFirst().orElse(null);
    }

    public static CommandService getCommandService() {
        return getServices().stream().filter(s -> s instanceof CommandService).map(s -> (CommandService) s).findFirst().orElse(null);
    }

    public static MenuService getMenuService() {
        return getServices().stream().filter(s -> s instanceof MenuService).map(s -> (MenuService) s).findFirst().orElse(null);
    }

    public static TabListService getTabListService() {
        return getServices().stream().filter(s -> s instanceof TabListService).map(s -> (TabListService) s).findFirst().orElse(null);
    }

    public static ScoreboardService getScoreboardService() {
        return getServices().stream().filter(s -> s instanceof ScoreboardService).map(s -> (ScoreboardService) s).findFirst().orElse(null);
    }

    public static NametagService getNametagService() {
        return getServices().stream().filter(s -> s instanceof NametagService).map(s -> (NametagService) s).findFirst().orElse(null);
    }

    public static SchedulerService getSchedulerService() {
        return getServices().stream().filter(s -> s instanceof SchedulerService).map(s -> (SchedulerService) s).findFirst().orElse(null);
    }

    public static StoreService getStoreService() {
        return getServices().stream().filter(s -> s instanceof StoreService).map(s -> (StoreService) s).findFirst().orElse(null);
    }

    public static NpcService getNpcService() {
        return getServices().stream().filter(s -> s instanceof NpcService).map(s -> (NpcService) s).findFirst().orElse(null);
    }

    public static HologramService getHologramService() {
        return getServices().stream().filter(s -> s instanceof HologramService).map(s -> (HologramService) s).findFirst().orElse(null);
    }

    public static PromptService getPromptService() {
        return getServices().stream().filter(s -> s instanceof PromptService).map(s -> (PromptService) s).findFirst().orElse(null);
    }

}
