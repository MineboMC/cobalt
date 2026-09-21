package net.minebo.cobalt.service.npc.menu;

import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.button.BackButton;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcAction;
import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

public final class NpcActionsMenu extends Menu {

    private final NpcService service;
    private final Npc npc;

    public NpcActionsMenu(NpcService service, Npc npc) {
        this.service = service;
        this.npc = npc;
        setTitle("<aqua>NPC Actions");
        setSize(27);
        setUpdateAfterClick(true);
        build();
    }

    private void build() {
        clearButtons();
        int slot = 0;
        for (int i = 0; i < npc.actions().size() && slot < 18; i++) {
            NpcAction action = npc.actions().get(i);
            int index = i;
            setButton(slot++, new Button()
                    .setName("<yellow>#" + index + " " + action.type().name())
                    .setLines(
                            "<white>" + action.value(),
                            "<gray>Click to remove this action."
                    )
                    .setMaterial(material(action))
                    .addClickAction(ClickType.LEFT, player -> {
                        npc.removeAction(index);
                        service.save(npc);
                        new NpcActionsMenu(service, npc).openMenu(player);
                    })
                    .addClickAction(ClickType.RIGHT, player -> {
                        npc.removeAction(index);
                        service.save(npc);
                        new NpcActionsMenu(service, npc).openMenu(player);
                    }));
        }
        setButton(22, new Button()
                .setName("<red>Clear all")
                .setLines("<gray>Remove every click action.")
                .setMaterial(Material.BARRIER)
                .addClickAction(ClickType.LEFT, player -> {
                    npc.actions().clear();
                    service.save(npc);
                    new NpcActionsMenu(service, npc).openMenu(player);
                }));
        setButton(26, new BackButton(null, new NpcEditorMenu(service, npc)));
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }

    private static Material material(NpcAction action) {
        return switch (action.type()) {
            case MESSAGE -> Material.PAPER;
            case PLAYER_COMMAND -> Material.COMMAND_BLOCK;
            case CONSOLE_COMMAND -> Material.REPEATING_COMMAND_BLOCK;
        };
    }
}