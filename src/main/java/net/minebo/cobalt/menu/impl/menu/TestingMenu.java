package net.minebo.cobalt.menu.impl.menu;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class TestingMenu extends Menu {

    public TestingMenu() {
        setTitle("Testing Menu");
        setSize(18);
        setAutoUpdate(true);

        setButton(0, new Button()
                .setName("Test")
                .setLines(
                        "<green>Updating",
                        "<yellow>Button",
                        "<light_purple>Current System Milis: <white>" + System.currentTimeMillis()
                )

                .setMaterial(Material.BOOK)
                .setAmount(1)
                .addClickAction(ClickType.LEFT, player -> player.sendMessage(ChatColor.GREEN + "Left Click test"))
                .addClickAction(ClickType.RIGHT, player -> player.sendMessage(ChatColor.GREEN + "Right Click test"))
        );
    }
}
