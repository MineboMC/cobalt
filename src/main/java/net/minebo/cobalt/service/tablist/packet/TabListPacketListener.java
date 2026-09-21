package net.minebo.cobalt.service.tablist.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.minebo.cobalt.service.tablist.state.PlayerTabState;
import net.minebo.cobalt.service.tablist.state.TabListTracker;
import org.bukkit.entity.Player;

public final class TabListPacketListener extends PacketListenerAbstract {

    private final TabListTracker tracker;

    public TabListPacketListener(TabListTracker tracker) {
        super(PacketListenerPriority.HIGH);
        this.tracker = tracker;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            return;
        }

        Object channelPlayer = event.getPlayer();
        if (!(channelPlayer instanceof Player player)) {
            return;
        }

        PlayerTabState state = tracker.get(player).orElse(null);
        if (state == null || state.slotCount() <= 0) {
            return;
        }

        WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);
        boolean changed = false;
        for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry : wrapper.getEntries()) {
            if (state.owns(entry.getProfileId())) {
                continue;
            }
            if (entry.isListed()) {
                entry.setListed(false);
                changed = true;
            }
        }
        if (changed) {
            event.markForReEncode(true);
        }
    }
}