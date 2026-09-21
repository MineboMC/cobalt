package net.minebo.cobalt.service.tablist;

import com.github.retrooper.packetevents.PacketEvents;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.tablist.listener.TabListListener;
import net.minebo.cobalt.service.tablist.example.ExampleTabListProvider;
import net.minebo.cobalt.service.tablist.packet.TabListPacketListener;
import net.minebo.cobalt.service.tablist.packet.TabListPacketSender;
import net.minebo.cobalt.service.tablist.registry.TabListProviderRegistry;
import net.minebo.cobalt.service.tablist.state.PlayerTabState;
import net.minebo.cobalt.service.tablist.state.TabListTracker;
import net.minebo.cobalt.service.tablist.task.TabListUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;

public final class TabListService extends CService {

    private TabListProviderRegistry registry;
    private TabListTracker tracker;
    private TabListPacketSender packets;
    private TabListListener bukkitListener;
    private TabListPacketListener packetListener;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Tablists";
    }

    @Override
    public void onEnable() {
        registry = new TabListProviderRegistry();
        tracker = new TabListTracker();
        packets = new TabListPacketSender();
        bukkitListener = new TabListListener(Cobalt.getInstance(), this);
        packetListener = new TabListPacketListener(tracker);

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
            clear(player);
        }
        tracker.clear();
    }

    public void registerProvider(TabListProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(TabListProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<TabListProvider> getProviders() {
        return registry.all();
    }

    public Optional<TabListProvider> getActiveProvider() {
        return Optional.ofNullable(registry.select());
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

        TabListProvider provider = registry.select();
        if (provider == null) {
            clear(player);
            return;
        }

        TabLayout layout;
        Component header;
        Component footer;
        try {
            layout = provider.provide(player);
            header = provider.getHeader(player);
            footer = provider.getFooter(player);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[TabList] Provider " + provider.getClass().getSimpleName()
                    + " failed for " + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            clear(player);
            return;
        }

        if (layout == null) {
            clear(player);
            return;
        }

        PlayerTabState state = tracker.getOrCreate(player);
        int oldCount = state.slotCount();
        int slotCount = layout.slotCount();
        state.setSlotCount(slotCount);

        if (!state.isApplied() || oldCount != slotCount) {
            applyLayout(state, layout);
            packets.resize(player, state, oldCount, slotCount);
        } else {
            for (int index = 0; index < slotCount; index++) {
                TabSlot next = layout.slot(index);
                TabSlot previous = state.slot(index);
                if (!previous.sameVisuals(next)) {
                    state.setSlot(index, next);
                    packets.updateSlot(player, state, index, previous, next);
                }
            }
        }

        if (state.headerFooterChanged(header, footer)) {
            packets.sendHeaderFooter(player, header, footer);
            state.setHeaderFooter(header, footer);
        }

        hideOnlinePlayers(player, state);
    }

    public void clear(Player player) {
        PlayerTabState state = tracker.get(player).orElse(null);
        if (state == null) {
            return;
        }
        if (player.isOnline()) {
            packets.removeLayout(player, state);
            packets.sendHeaderFooter(player, Component.empty(), Component.empty());
        }
        state.setSlotCount(0);
        state.setHeaderFooter(Component.empty(), Component.empty());
    }

    public void handleQuit(Player player) {
        tracker.remove(player);
    }

    private void applyLayout(PlayerTabState state, TabLayout layout) {
        for (int index = 0; index < layout.slotCount(); index++) {
            state.setSlot(index, layout.slot(index));
        }
    }

    private void hideOnlinePlayers(Player viewer, PlayerTabState state) {
        if (state.slotCount() <= 0) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            packets.hidePlayer(viewer, online.getUniqueId());
        }
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new TabListUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                TabListUpdateTask.PERIOD_TICKS,
                TabListUpdateTask.PERIOD_TICKS
        );
    }
}