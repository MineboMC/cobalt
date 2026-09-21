package net.minebo.cobalt.service.menu.impl.button;

import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class BackButton extends Button {

    public BackButton(Player player, Menu menu) {
        this.setName("<red>Back");
        this.setLines("<gray>Click to go back.");
        this.setMaterial(Material.RED_DYE);

        this.addClickAction(ClickType.LEFT, menu::openMenu);
        this.addClickAction(ClickType.RIGHT, menu::openMenu);
    }

}
