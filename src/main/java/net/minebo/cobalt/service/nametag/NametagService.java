package net.minebo.cobalt.service.nametag;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.nametag.listener.NametagListener;
import net.minebo.cobalt.service.nametag.packet.NametagPacketSender;
import net.minebo.cobalt.service.nametag.registry.NametagProviderRegistry;
import net.minebo.cobalt.service.nametag.state.NametagTracker;
import net.minebo.cobalt.service.nametag.state.ViewerNametagState;
import net.minebo.cobalt.service.nametag.task.NametagUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NametagService extends CService {

    private NametagProviderRegistry registry;
    private NametagTracker tracker;
    private NametagPacketSender packets;
    private NametagListener bukkitListener;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Nametags";
    }

    @Override
    public void onEnable() {
        registry = new NametagProviderRegistry();
        tracker = new NametagTracker();
        packets = new NametagPacketSender();
        bukkitListener = new NametagListener(this);

        Bukkit.getPluginManager().registerEvents(bukkitListener, Cobalt.getInstance());
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
        HandlerList.unregisterAll(bukkitListener);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            clear(viewer);
        }
        tracker.clear();
    }

    public void registerProvider(NametagProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(NametagProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<NametagProvider> getProviders() {
        return registry.all();
    }

    public Optional<NametagProvider> getActiveProvider() {
        return Optional.ofNullable(registry.select());
    }

    public void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refresh(viewer);
        }
    }

    public void refresh(Player viewer) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            refresh(viewer, target);
        }
    }

    public void refreshTarget(Player target) {
        if (target == null || !target.isOnline()) {
            return;
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refresh(viewer, target);
        }
    }

    public void refresh(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) {
            return;
        }

        NametagProvider provider = registry.select();
        if (provider == null) {
            removePair(viewer, target.getUniqueId());
            return;
        }

        Nametag next;
        try {
            next = provider.provide(viewer, target);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Nametag] Provider " + provider.getClass().getSimpleName()
                    + " failed for " + viewer.getName() + " -> " + target.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            removePair(viewer, target.getUniqueId());
            return;
        }

        if (next == null) {
            removePair(viewer, target.getUniqueId());
            return;
        }

        ViewerNametagState state = tracker.getOrCreate(viewer);
        Nametag previous = state.get(target.getUniqueId());
        if (previous == null) {
            packets.create(viewer, target, next);
            state.put(target.getUniqueId(), next);
            return;
        }
        if (!previous.equals(next)) {
            packets.update(viewer, target, next);
            state.put(target.getUniqueId(), next);
        }
    }

    public void clear(Player viewer) {
        ViewerNametagState state = tracker.get(viewer).orElse(null);
        if (state == null) {
            return;
        }
        if (viewer.isOnline()) {
            for (UUID targetId : state.all().keySet()) {
                packets.remove(viewer, targetId);
            }
        }
        state.all().clear();
    }

    public void handleQuit(Player player) {
        clear(player);
        tracker.remove(player);
        UUID left = player.getUniqueId();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            removePair(viewer, left);
        }
    }

    private void removePair(Player viewer, UUID targetId) {
        ViewerNametagState state = tracker.get(viewer).orElse(null);
        if (state == null) {
            return;
        }
        if (state.remove(targetId) != null && viewer.isOnline()) {
            packets.remove(viewer, targetId);
        }
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new NametagUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                NametagUpdateTask.PERIOD_TICKS,
                NametagUpdateTask.PERIOD_TICKS
        );
    }
}