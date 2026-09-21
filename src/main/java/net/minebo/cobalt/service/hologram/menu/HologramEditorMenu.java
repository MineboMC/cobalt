package net.minebo.cobalt.service.hologram.menu;

import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.menu.ConfirmMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

public final class HologramEditorMenu extends Menu {

    private final HologramService service;
    private final Hologram hologram;

    public HologramEditorMenu(HologramService service, Hologram hologram) {
        this.service = service;
        this.hologram = hologram;
        setTitle(hologram == null ? "<yellow>Holograms" : "<yellow>" + hologram.id());
        setSize(27);
        setUpdateAfterClick(true);
        build();
    }

    private void build() {
        clearButtons();
        if (hologram == null) {
            int slot = 0;
            for (Hologram listed : service.registry().all()) {
                if (listed.internal()) continue;
                if (slot >= 26) break;
                Hologram target = listed;
                setButton(slot++, new Button()
                        .setName("<yellow>" + listed.id())
                        .setLines("<gray>" + listed.lines().size() + " lines")
                        .setMaterial(Material.ARMOR_STAND)
                        .addClickAction(ClickType.LEFT, player -> new HologramEditorMenu(service, target).openMenu(player)));
            }
            fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
            return;
        }

        setButton(11, new Button()
                .setName("<aqua>Move here")
                .setMaterial(Material.ENDER_PEARL)
                .addClickAction(ClickType.LEFT, player -> service.teleportHere(hologram, player)));

        setButton(13, new Button()
                .setName("<gold>Lines")
                .setLines("<white>" + hologram.lines().size() + " line(s)", "<gray>/holo line add " + hologram.id() + " &aText")
                .setMaterial(Material.PAPER)
                .addClickAction(ClickType.LEFT, player -> new HologramLinesMenu(service, hologram).openMenu(player)));

        setButton(15, new Button()
                .setName("<aqua>View distance")
                .setLines("<white>" + hologram.viewDistance() + " blocks")
                .setMaterial(Material.SPYGLASS));

        setButton(22, new Button()
                .setName("<red>Delete")
                .setMaterial(Material.TNT)
                .addClickAction(ClickType.LEFT, player -> new ConfirmMenu("Delete " + hologram.id(), confirmed -> {
                    if (confirmed) service.delete(hologram);
                    else new HologramEditorMenu(service, hologram).openMenu(player);
                }).openMenu(player)));

        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }
}