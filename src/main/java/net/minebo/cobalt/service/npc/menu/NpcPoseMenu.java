package net.minebo.cobalt.service.npc.menu;

import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.button.BackButton;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcPose;
import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

public final class NpcPoseMenu extends Menu {

    private final NpcService service;
    private final Npc npc;

    public NpcPoseMenu(NpcService service, Npc npc) {
        this.service = service;
        this.npc = npc;
        setTitle("<light_purple>NPC Pose");
        setSize(27);
        setUpdateAfterClick(true);
        build();
    }

    private void build() {
        clearButtons();
        pose(10, NpcPose.STANDING, Material.ARMOR_STAND);
        pose(11, NpcPose.SNEAKING, Material.GOLDEN_BOOTS);
        pose(12, NpcPose.SPRINTING, Material.SUGAR);
        pose(13, NpcPose.SWIMMING, Material.HEART_OF_THE_SEA);
        pose(14, NpcPose.SLEEPING, Material.RED_BED);
        pose(15, NpcPose.SITTING, Material.OAK_STAIRS);
        setButton(22, new BackButton(null, new NpcEditorMenu(service, npc)));
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }

    private void pose(int slot, NpcPose pose, Material material) {
        boolean selected = npc.pose() == pose;
        setButton(slot, new Button()
                .setName((selected ? "<green>" : "<yellow>") + pose.name())
                .setLines(selected ? "<gray>Selected" : "<gray>Click to apply")
                .setMaterial(material)
                .addClickAction(ClickType.LEFT, player -> {
                    service.setPose(npc, pose);
                    new NpcPoseMenu(service, npc).openMenu(player);
                }));
    }
}