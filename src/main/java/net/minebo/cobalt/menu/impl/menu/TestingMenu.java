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

        buttons.put(0, new Button()
                .setName("#F4D654&lT#F6D552&le#F7D451&ls#F9D34F&lt#FAD14E&li#FCD04C&ln#FDCF4B&lg #FFCB3D&lB#FFC731&lu#FFC425&lt#FFC118&lt#FFBD0C&lo#FFBA00&ln")
                .setLines(() -> Arrays.asList(
                        "&aUpdating",
                        "&eButton",
                        "&dCurrent System Milis: &f" + System.currentTimeMillis()
                        )
                )

                .setMaterial(Material.BOOK)
                .setAmount(1)
                .addClickAction(ClickType.LEFT, player -> player.sendMessage(ChatColor.GREEN + "Left Click test"))
                .addClickAction(ClickType.RIGHT, player -> player.sendMessage(ChatColor.GREEN + "Right Click test"))
        );
    }
}
