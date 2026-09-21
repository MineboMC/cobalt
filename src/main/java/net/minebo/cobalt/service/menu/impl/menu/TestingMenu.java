package net.minebo.cobalt.service.menu.impl.menu;

import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class TestingMenu extends Menu {

    public TestingMenu() {
        setTitle("<yellow>Testing Menu");
        setSize(18);
        setAutoUpdate(true);

        setButton(0, new Button()
                .setName("<yellow>Test Button")
                .setLines(() -> List.of(
                        "<green>Updating",
                        "<yellow>Button",
                        "<light_purple>Current System Millis: <white>" + System.currentTimeMillis())
                )
                .setMaterial(Material.BOOK)
                .setAmount(1)
                .addClickAction(ClickType.LEFT, player -> player.sendMessage(Coloring.translateColors("<green>Left Click test")))
                .addClickAction(ClickType.RIGHT, player -> player.sendMessage(Coloring.translateColors("<green>Right Click test")))
        );
    }
}