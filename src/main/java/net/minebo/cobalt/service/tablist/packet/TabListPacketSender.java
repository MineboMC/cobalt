package net.minebo.cobalt.service.tablist.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.tablist.TabLayout;
import net.minebo.cobalt.service.tablist.TabSlot;
import net.minebo.cobalt.service.tablist.state.PlayerTabState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public final class TabListPacketSender {

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> ADD_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
    );

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> UPDATE_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
    );

    public void sendLayout(Player player, PlayerTabState state) {
        int count = state.slotCount();
        if (count <= 0) {
            return;
        }

        List<UUID> ids = new ArrayList<>(PlayerTabState.MAX_SLOTS);
        for (int index = 0; index < PlayerTabState.MAX_SLOTS; index++) {
            ids.add(state.slotId(index));
        }
        send(player, new WrapperPlayServerPlayerInfoRemove(ids));

        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(toInfo(state, index, state.slot(index)));
        }
        send(player, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, entries));
        state.setApplied(true);
    }

    public void updateSlot(Player player, PlayerTabState state, int index, TabSlot previous, TabSlot next) {
        if (!state.isApplied()) {
            sendLayout(player, state);
            return;
        }

        boolean skinChanged = previous == null || !previous.skin().equals(next.skin());
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = toInfo(state, index, next);
        if (skinChanged) {
            send(player, new WrapperPlayServerPlayerInfoRemove(List.of(state.slotId(index))));
            send(player, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, List.of(info)));
            return;
        }
        send(player, new WrapperPlayServerPlayerInfoUpdate(UPDATE_ACTIONS, List.of(info)));
    }

    public void sendHeaderFooter(Player player, Component header, Component footer) {
        player.sendPlayerListHeaderAndFooter(
                header == null ? Component.empty() : header,
                footer == null ? Component.empty() : footer
        );
    }

    public void hidePlayer(Player viewer, UUID target) {
        UserProfile profile = new UserProfile(target, "");
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile,
                false,
                0,
                GameMode.SURVIVAL,
                null,
                null,
                0
        );
        send(viewer, new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED),
                List.of(info)
        ));
    }

    public void removeLayout(Player player, PlayerTabState state) {
        List<UUID> ids = new ArrayList<>();
        for (int index = 0; index < PlayerTabState.MAX_SLOTS; index++) {
            ids.add(state.slotId(index));
        }
        send(player, new WrapperPlayServerPlayerInfoRemove(ids));
        state.setApplied(false);
    }

    public void resize(Player player, PlayerTabState state, int oldCount, int newCount) {
        if (newCount > 0) {
            sendLayout(player, state);
        } else {
            removeLayout(player, state);
        }
    }

    private WrapperPlayServerPlayerInfoUpdate.PlayerInfo toInfo(PlayerTabState state, int index, TabSlot slot) {
        UserProfile profile = new UserProfile(state.slotId(index), nameFor(index));
        slot.skin().apply(profile);
        Component text = slot.text() == null ? Component.empty() : slot.text();
        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile,
                true,
                slot.ping(),
                GameMode.SURVIVAL,
                text,
                null,
                TabLayout.listOrder(index)
        );
    }

    private static String nameFor(int index) {
        int column = index / TabLayout.ROWS;
        int row = index % TabLayout.ROWS;
        return String.format("%c%02d", 'A' + column, TabLayout.ROWS - 1 - row);
    }

    private void send(Player player, com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[TabList] Failed to send packet to "
                    + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}