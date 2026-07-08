package net.minebo.cobalt.menu.impl.menu;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

public class TestingMenu extends Menu {

    public TestingMenu() {
        setTitle(ColorUtil.translateColors("<yellow>Testing Menu"));
        setSize(18);
        setAutoUpdate(true);

        setButton(0, new Button()
                .setName("<yellow>Test Button")
                .setLines(
                        "<green>Updating",
                        "<yellow>Button",
                        "<light_purple>Current System Millis: <white>" + System.currentTimeMillis()
                )
                .setMaterial(Material.BOOK)
                .setAmount(1)
                .addClickAction(ClickType.LEFT, player -> player.sendMessage(ColorUtil.translateColors("<green>Left Click test")))
                .addClickAction(ClickType.RIGHT, player -> player.sendMessage(ColorUtil.translateColors("<green>Right Click test")))
        );
    }
}