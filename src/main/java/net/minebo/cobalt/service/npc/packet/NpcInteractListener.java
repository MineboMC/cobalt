package net.minebo.cobalt.service.npc.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcInteractListener extends PacketListenerAbstract {

    private final NpcService service;
    private final Map<UUID, Long> lastClick = new ConcurrentHashMap<>();

    public NpcInteractListener(NpcService service) {
        super(PacketListenerPriority.NORMAL);
        this.service = service;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return;
        }
        Npc npc = service.registry().byEntityId(packet.getEntityId());
        if (npc == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long previous = lastClick.put(player.getUniqueId(), now);
        if (previous != null && now - previous < 200L) {
            return;
        }
        Bukkit.getScheduler().runTask(Cobalt.getInstance(), () -> service.handleClick(player, npc));
    }
}