package net.minebo.cobalt.service.npc.menu;

import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.impl.button.BackButton;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class NpcEquipmentMenu extends Menu {

    private final NpcService service;
    private final Npc npc;

    public NpcEquipmentMenu(NpcService service, Npc npc) {
        this.service = service;
        this.npc = npc;
        setTitle("<gold>NPC Equipment");
        setSize(27);
        setUpdateAfterClick(false);
        build();
    }

    private void build() {
        clearButtons();
        setButton(10, equipment("Helmet", npc.equipment().helmet(), Material.LEATHER_HELMET, npc.equipment()::setHelmet));
        setButton(11, equipment("Chestplate", npc.equipment().chestplate(), Material.LEATHER_CHESTPLATE, npc.equipment()::setChestplate));
        setButton(12, equipment("Leggings", npc.equipment().leggings(), Material.LEATHER_LEGGINGS, npc.equipment()::setLeggings));
        setButton(13, equipment("Boots", npc.equipment().boots(), Material.LEATHER_BOOTS, npc.equipment()::setBoots));
        setButton(14, equipment("Hand", npc.equipment().hand(), Material.STICK, npc.equipment()::setHand));
        setButton(15, equipment("Off Hand", npc.equipment().offHand(), Material.SHIELD, npc.equipment()::setOffHand));
        setButton(22, new BackButton(null, new NpcEditorMenu(service, npc)));
        fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);
    }

    private Button equipment(String name, ItemStack current, Material fallback, SlotSetter setter) {
        Material material = current == null ? fallback : current.getType();
        return new Button()
                .setName("<yellow>" + name)
                .setLines(
                        current == null ? "<gray>Empty" : "<white>" + current.getType().name(),
                        "<gray>Drag an item onto this slot to set it.",
                        "<gray>Click with an empty cursor to take it off."
                )
                .setMaterial(material)
                .addClickAction(ClickType.LEFT, player -> apply(player, current, setter))
                .addClickAction(ClickType.RIGHT, player -> apply(player, current, setter))
                .addClickAction(ClickType.SHIFT_LEFT, player -> apply(player, current, setter))
                .addClickAction(ClickType.SHIFT_RIGHT, player -> apply(player, current, setter));
    }

    private void apply(Player player, ItemStack current, SlotSetter setter) {
        ItemStack cursor = player.getItemOnCursor();
        boolean cursorEmpty = cursor == null || cursor.getType().isAir();
        if (!cursorEmpty) {
            ItemStack applied = cursor.clone();
            applied.setAmount(1);
            setter.set(applied);
            if (cursor.getAmount() <= 1) {
                player.setItemOnCursor(current == null ? null : current.clone());
            } else {
                cursor.setAmount(cursor.getAmount() - 1);
                player.setItemOnCursor(cursor);
            }
        } else if (current != null) {
            player.setItemOnCursor(current.clone());
            setter.set(null);
        }
        service.refreshEquipment(npc);
        new NpcEquipmentMenu(service, npc).openMenu(player);
    }

    @FunctionalInterface
    private interface SlotSetter {
        void set(ItemStack item);
    }
}