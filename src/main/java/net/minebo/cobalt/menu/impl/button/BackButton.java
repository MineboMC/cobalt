package net.minebo.cobalt.menu.impl.button;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class BackButton extends Button {

    public BackButton(Player player, Menu menu) {
        this.setName(ColorUtil.translateColors("&cBack"));
        this.setLines(ColorUtil.translateColors("&7Click to go back."));
        this.setMaterial(Material.RED_DYE);

        this.addClickAction(ClickType.LEFT, menu::openMenu);
        this.addClickAction(ClickType.RIGHT, menu::openMenu);
    }

}
