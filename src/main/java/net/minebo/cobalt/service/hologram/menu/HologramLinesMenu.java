package net.minebo.cobalt.service.hologram.menu;

import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.hologram.conversation.HologramLineConversation;
import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.button.BackButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

public final class HologramLinesMenu extends Menu {

    private final HologramService service;
    private final Hologram hologram;
    private final Menu back;
    private final Runnable onEdited;

    public HologramLinesMenu(HologramService service, Hologram hologram) {
        this(service, hologram, new HologramEditorMenu(service, hologram), null);
    }

    public HologramLinesMenu(HologramService service, Hologram hologram, Menu back) {
        this(service, hologram, back, null);
    }

    public HologramLinesMenu(HologramService service, Hologram hologram, Menu back, Runnable onEdited) {
        this.service = service;
        this.hologram = hologram;
        this.back = back;
        this.onEdited = onEdited;
        setTitle("<gold>Lines: " + hologram.id());
        setSize(27);
        setAutoUpdate(true); // saves me time in the way that i dont need to spend more time on this -- kab
        setUpdateAfterClick(true);
        build();
    }

    private void build() {
        clearButtons();
        int slot = 0;
        for (int i = 0; i < hologram.lines().size() && slot < 18; i++) {
            int index = i;
            String text = hologram.lines().get(i);
            setButton(slot++, new Button()
                    .setName("<yellow>#" + index)
                    .setLines("<white>" + text, "<gray>Left click to edit", "<gray>Right click to remove")
                    .setMaterial(Material.NAME_TAG)
                    .addClickAction(ClickType.LEFT, player -> HologramLineConversation.editLine(
                            service, hologram, index, player, new HologramLinesMenu(service, hologram, back, onEdited), onEdited))
                    .addClickAction(ClickType.RIGHT, player -> {
                        service.removeLine(hologram, index);
                        if (onEdited != null) onEdited.run();
                        new HologramLinesMenu(service, hologram, back, onEdited).openMenu(player);
                    }));
        }
        setButton(22, new Button()
                .setName("<green>Add line")
                .setLines("<gray>Click, then type the line in chat.", "<gray>Type cancel to abort.")
                .setMaterial(Material.EMERALD)
                .addClickAction(ClickType.LEFT, player -> HologramLineConversation.addLine(
                        service, hologram, player, new HologramLinesMenu(service, hologram, back, onEdited), onEdited)));
        setButton(26, new BackButton(null, back));
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }
}