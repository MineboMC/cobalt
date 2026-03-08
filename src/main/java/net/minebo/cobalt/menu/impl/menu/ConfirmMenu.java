package net.minebo.cobalt.menu.impl.menu;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Consumer;

public class ConfirmMenu {

    private final String title;
    private final Consumer<Boolean> callback;

    public ConfirmMenu(String title, Consumer<Boolean> callback) {
        this.title = title;
        this.callback = callback;
    }

    public void openMenu(Player player) {
        Menu menu = new Menu()
                .setTitle("&cConfirm: " + title)
                .setSize(27);

        // Confirm button (green wool) at slot 11
        Button confirmButton = new Button()
                .setMaterial(Material.GREEN_WOOL)
                .setAmount(1)
                .setName("&a&lConfirm")
                .setLines(
                        "&7Click to confirm this action."
                )
                .addClickAction(ClickType.LEFT, (p) -> {
                    p.closeInventory();
                    callback.accept(true);
                })
                .addClickAction(ClickType.RIGHT, (p) -> {
                    p.closeInventory();
                    callback.accept(true);
                });

        menu.setButton(11, confirmButton);

        // Cancel button (red wool) at slot 15
        Button cancelButton = new Button()
                .setMaterial(Material.RED_WOOL)
                .setAmount(1)
                .setName("&c&lCancel")
                .setLines(
                        "&7Click to cancel this action."
                )
                .addClickAction(ClickType.LEFT, (p) -> {
                    p.closeInventory();
                    callback.accept(false);
                })
                .addClickAction(ClickType.RIGHT, (p) -> {
                    p.closeInventory();
                    callback.accept(false);
                });

        menu.setButton(15, cancelButton);

        // Fill empty slots with gray glass pane
        menu.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, true);

        menu.openMenu(player);
    }
}