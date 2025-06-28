package net.minebo.cobalt.menu.impl;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TestingMenu extends Menu {

    public TestingMenu() {
        setTitle("Testing Menu");
        setSize(18);
        setAutoUpdate(true);

        buttons.put(0, new Button()
                .setName("Button")
                .setLines("Testing", "Button")
                .setMaterial(Material.BARRIER)
                .setAmount(1)
                .setClickAction(player -> player.sendMessage(ChatColor.GREEN + "Testing"))
        );
    }
}
