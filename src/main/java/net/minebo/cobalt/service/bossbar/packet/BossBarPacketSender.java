package net.minebo.cobalt.service.bossbar.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.bossbar.state.ShownBar;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.UUID;

public final class BossBarPacketSender {

    private static final Component EMPTY = Component.empty();

    public void sendAdd(Player player, UUID barId, ShownBar bar) {
        WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.ADD);
        packet.setTitle(title(bar));
        packet.setHealth(bar.health());
        packet.setColor(bar.color());
        packet.setOverlay(bar.overlay());
        packet.setFlags(copyFlags(bar.flags()));
        send(player, packet);
    }

    public void sendUpdates(Player player, UUID barId, ShownBar previous, ShownBar next) {
        if (!previous.sameTitle(next)) {
            WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.UPDATE_TITLE);
            packet.setTitle(title(next));
            send(player, packet);
        }
        if (!previous.sameHealth(next)) {
            WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.UPDATE_HEALTH);
            packet.setHealth(next.health());
            send(player, packet);
        }
        if (!previous.sameStyle(next)) {
            WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.UPDATE_STYLE);
            packet.setColor(next.color());
            packet.setOverlay(next.overlay());
            send(player, packet);
        }
        if (!previous.sameFlags(next)) {
            WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.UPDATE_FLAGS);
            packet.setFlags(copyFlags(next.flags()));
            send(player, packet);
        }
    }

    public void sendRemove(Player player, UUID barId) {
        send(player, new WrapperPlayServerBossBar(barId, WrapperPlayServerBossBar.Action.REMOVE));
    }

    private void send(Player player, WrapperPlayServerBossBar packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private static Component title(ShownBar bar) {
        Component title = bar.title();
        return title == null ? EMPTY : title;
    }

    private static EnumSet<BossBar.Flag> copyFlags(EnumSet<BossBar.Flag> flags) {
        return flags == null || flags.isEmpty()
                ? EnumSet.noneOf(BossBar.Flag.class)
                : EnumSet.copyOf(flags);
    }
}