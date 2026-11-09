package net.minebo.cobalt.service.nametag.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.nametag.NametagService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Follows the server spawning and despawning players for a viewer, so the extra
 * nametag lines riding them come and go with the player entity.
 */
public final class NametagPacketListener extends PacketListenerAbstract {

    private final NametagService service;
    private final NametagPacketSender packets;

    public NametagPacketListener(NametagService service, NametagPacketSender packets) {
        super(PacketListenerPriority.HIGHEST);
        this.service = service;
        this.packets = packets;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);
            if (packet.getEntityType() != EntityTypes.PLAYER) {
                return;
            }
            UUID target = packets.targetOf(packet.getEntityId());
            if (target != null) {
                event.getPostTasks().add(() -> Bukkit.getScheduler().runTask(Cobalt.getInstance(),
                        () -> service.showLines(viewer, target)));
            }
        } else if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
            for (int entityId : packet.getEntityIds()) {
                UUID target = packets.targetOf(entityId);
                if (target != null) {
                    packets.hideLines(viewer, target);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);
            UUID target = packets.targetOf(packet.getEntityId());
            if (target != null && packets.isShowingLines(viewer, target)) {
                packet.setPassengers(packets.withLines(target, packet.getPassengers()));
                event.markForReEncode(true);
            }
        }
    }
}
