package net.minebo.cobalt.service.bossbar;

import com.github.retrooper.packetevents.PacketEvents;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.bossbar.example.ExampleBossBarProvider;
import net.minebo.cobalt.service.bossbar.listener.BossBarListener;
import net.minebo.cobalt.service.bossbar.packet.BossBarPacketListener;
import net.minebo.cobalt.service.bossbar.packet.BossBarPacketSender;
import net.minebo.cobalt.service.bossbar.registry.BossBarProviderRegistry;
import net.minebo.cobalt.service.bossbar.state.BossBarTracker;
import net.minebo.cobalt.service.bossbar.state.PlayerBarState;
import net.minebo.cobalt.service.bossbar.state.ShownBar;
import net.minebo.cobalt.service.bossbar.task.BossBarUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BossBarService extends CService {

    private BossBarProviderRegistry registry;
    private BossBarTracker tracker;
    private BossBarPacketSender packets;
    private BossBarListener bukkitListener;
    private BossBarPacketListener packetListener;

    private BukkitTask task;

    @Override
    public String getName() {
        return "Bossbars";
    }

    @Override
    public void onEnable() {
        registry = new BossBarProviderRegistry();
        tracker = new BossBarTracker();
        packets = new BossBarPacketSender();
        bukkitListener = new BossBarListener(Cobalt.getInstance(), this);
        packetListener = new BossBarPacketListener(tracker);

        Bukkit.getPluginManager().registerEvents(bukkitListener, Cobalt.getInstance());
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        startTask();

        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        HandlerList.unregisterAll(bukkitListener);

        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
        tracker.clear();
    }

    public void registerProvider(BossBarProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(BossBarProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<BossBarProvider> getProviders() {
        return registry.all();
    }

    public Optional<BossBarProvider> getActiveProvider() {
        return Optional.ofNullable(registry.select());
    }

    public void setSuppressForeignBars(boolean suppress) {
        packetListener.setSuppressForeignBars(suppress);
    }

    public boolean isSuppressForeignBars() {
        return packetListener.isSuppressForeignBars();
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        BossBarProvider provider = registry.select();
        if (provider == null) {
            hide(player);
            return;
        }

        ShownBar next;
        try {
            next = ShownBar.from(provider, player);
        } catch (Exception exception) {
            hide(player);
            return;
        }

        PlayerBarState state = tracker.getOrCreate(player);
        if (!state.isVisible()) {
            packets.sendAdd(player, state.barId(), next);
            state.setShown(next);
            return;
        }

        ShownBar previous = state.shown();
        if (!previous.sameVisuals(next)) {
            packets.sendUpdates(player, state.barId(), previous, next);
            state.setShown(next);
        }
    }

    public void hide(Player player) {
        if (player == null) {
            return;
        }

        PlayerBarState state = tracker.remove(player);
        if (state != null && state.isVisible() && player.isOnline()) {
            packets.sendRemove(player, state.barId());
        }
    }

    public void handleQuit(Player player) {
        tracker.remove(player);
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new BossBarUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                BossBarUpdateTask.PERIOD_TICKS,
                BossBarUpdateTask.PERIOD_TICKS
        );
    }
}