package net.minebo.cobalt.service.npc.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.menu.HologramLinesMenu;
import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.menu.ConfirmMenu;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcService;
import net.minebo.cobalt.service.npc.NpcSkinMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public final class NpcEditorMenu extends Menu {

    private final NpcService service;
    private final Npc npc;

    public NpcEditorMenu(NpcService service, Npc npc) {
        this.service = service;
        this.npc = npc;
        setTitle(npc == null ? "<yellow>NPCs" : "<yellow>" + npc.name());
        setSize(27);
        setUpdateAfterClick(true);
        build();
    }

    private void build() {
        clearButtons();
        if (npc == null) {
            int slot = 0;
            for (Npc listed : service.registry().all()) {
                if (slot >= 26) {
                    break;
                }
                Npc target = listed;
                setButton(slot++, new Button()
                        .setName("<yellow>" + listed.name())
                        .setLines("<gray>" + listed.type().name())
                        .setMaterial(listed.isPlayerNpc() ? Material.PLAYER_HEAD : Material.ARMOR_STAND)
                        .addClickAction(ClickType.LEFT, player -> new NpcEditorMenu(service, target).openMenu(player)));
            }
            fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
            return;
        }

        setButton(10, new Button()
                .setName("<aqua>Move here")
                .setMaterial(Material.ENDER_PEARL)
                .addClickAction(ClickType.LEFT, player -> service.teleportHere(npc, player)));

        setButton(11, new Button()
                .setName("<gold>Hologram lines")
                .setLines(() -> List.of( npc.hologramLines().isEmpty() ? "<gray>empty" : "<white>" + npc.hologramLines().getFirst()))
                .setMaterial(Material.NAME_TAG)
                .addClickAction(ClickType.LEFT, player -> {
                    Hologram hologram = service.ensureNameHologram(npc);
                    if (hologram == null || service.holograms() == null) {
                        player.sendMessage(Component.text("Hologram service is not hooked.", NamedTextColor.RED));
                        return;
                    }
                    new HologramLinesMenu(service.holograms(), hologram, new NpcEditorMenu(service, npc), () -> service.captureHologram(npc)).openMenu(player);
                }));

        if (npc.isPlayerNpc()) {
            setButton(12, new Button()
                    .setName("<yellow>Skin")
                    .setLines("<gray>" + npc.skinMode().name(), "<aqua>Left Click to type a player's name", "<red>Right Click to set to viewer's skin")
                    .setMaterial(Material.PLAYER_HEAD)
                    .addClickAction(ClickType.LEFT, this::promptSkinName)
                    .addClickAction(ClickType.RIGHT, player -> service.setSkinMode(npc, NpcSkinMode.VIEWER)));
        }

        setButton(13, new Button()
                .setName("<gold>Equipment")
                .setMaterial(Material.IRON_CHESTPLATE)
                .addClickAction(ClickType.LEFT, player -> new NpcEquipmentMenu(service, npc).openMenu(player)));

        setButton(14, new Button()
                .setName("<light_purple>Pose")
                .setLines("<white>" + npc.pose().name())
                .setMaterial(Material.ARMOR_STAND)
                .addClickAction(ClickType.LEFT, player -> new NpcPoseMenu(service, npc).openMenu(player)));

        setButton(15, new Button()
                .setName("<aqua>Actions")
                .setLines("<white>" + npc.actions().size() + " set")
                .setMaterial(Material.COMMAND_BLOCK)
                .addClickAction(ClickType.LEFT, player -> new NpcActionsMenu(service, npc).openMenu(player)));

        setButton(16, new Button()
                .setName(npc.lookAtPlayers() ? "<green>Look at players" : "<red>Look at players")
                .setLines("<white>" + npc.lookDistance() + " blocks", "<aqua>Left Click to toggle", "<yellow>Right Click to set the distance")
                .setMaterial(npc.lookAtPlayers() ? Material.ENDER_EYE : Material.ENDER_PEARL)
                .addClickAction(ClickType.LEFT, player -> {
                    npc.setLookAtPlayers(!npc.lookAtPlayers());
                    service.save(npc);
                    new NpcEditorMenu(service, npc).openMenu(player);
                })
                .addClickAction(ClickType.RIGHT, this::promptLookDistance));

        setButton(22, new Button()
                .setName("<red>Delete")
                .setMaterial(Material.TNT)
                .addClickAction(ClickType.LEFT, player -> new ConfirmMenu("Delete " + npc.name(), confirmed -> {
                    if (confirmed) {
                        service.delete(npc);
                        service.selection().clear(player);
                    } else {
                        new NpcEditorMenu(service, npc).openMenu(player);
                    }
                }).openMenu(player)));

        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }

    private void promptLookDistance(Player player) {
        player.closeInventory();

        CobaltAPI.getPromptService().ask(player,
                "<yellow>Type how many blocks away the NPC should look at players from, or <red>cancel</red> to go back.",
                input -> applyLookDistance(player, input),
                () -> new NpcEditorMenu(service, npc).openMenu(player));
    }

    private void applyLookDistance(Player editor, String input) {
        double distance;
        try {
            distance = Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            editor.sendMessage(Component.text("'" + input + "' isn't a number.", NamedTextColor.RED));
            new NpcEditorMenu(service, npc).openMenu(editor);
            return;
        }
        if (!Double.isFinite(distance) || distance < 1.0D) {
            editor.sendMessage(Component.text("The distance has to be at least 1 block.", NamedTextColor.RED));
            new NpcEditorMenu(service, npc).openMenu(editor);
            return;
        }

        npc.setLookDistance(distance);
        service.save(npc);
        editor.sendMessage(Component.text(npc.name() + " now looks at players within " + npc.lookDistance() + " blocks.", NamedTextColor.GREEN));
        new NpcEditorMenu(service, npc).openMenu(editor);
    }

    private void promptSkinName(Player player) {
        player.closeInventory();

        CobaltAPI.getPromptService().ask(player,
                "<yellow>Type the name of the player whose skin you want, or <red>cancel</red> to go back.",
                name -> applySkin(player, name),
                () -> {
                    player.sendMessage(Component.text("Skin change cancelled.", NamedTextColor.GRAY));
                    new NpcEditorMenu(service, npc).openMenu(player);
                });
    }

    private void applySkin(Player editor, String name) {
        if (name.isEmpty()) {
            editor.sendMessage(Component.text("You didn't enter a name.", NamedTextColor.RED));
            new NpcEditorMenu(service, npc).openMenu(editor);
            return;
        }

        CobaltAPI.getNpcService().setSkinFromUsername(npc, name, editor);
        editor.sendMessage(Component.text("Fetching skin for " + name + "...", NamedTextColor.GRAY));

        new NpcEditorMenu(service, npc).openMenu(editor);
    }
}