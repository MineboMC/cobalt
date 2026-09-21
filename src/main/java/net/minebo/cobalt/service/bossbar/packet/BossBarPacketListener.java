package net.minebo.cobalt.service.bossbar.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import lombok.Getter;
import lombok.Setter;
import net.minebo.cobalt.service.bossbar.state.BossBarTracker;
import org.bukkit.entity.Player;

public final class BossBarPacketListener extends PacketListenerAbstract {

    private final BossBarTracker tracker;
    @Getter
    @Setter
    private volatile boolean suppressForeignBars;

    public BossBarPacketListener(BossBarTracker tracker) {
        super(PacketListenerPriority.HIGH);
        this.tracker = tracker;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!suppressForeignBars) {
            return;
        }
        if (event.getPacketType() != PacketType.Play.Server.BOSS_BAR) {
            return;
        }

        Object channelPlayer = event.getPlayer();
        if (!(channelPlayer instanceof Player player)) {
            return;
        }

        tracker.get(player).ifPresent(state -> {
            WrapperPlayServerBossBar wrapper = new WrapperPlayServerBossBar(event);
            if (!state.barId().equals(wrapper.getUUID())) {
                event.setCancelled(true);
            }
        });
    }
}