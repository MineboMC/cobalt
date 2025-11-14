package net.minebo.cobalt.menu.construct;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class AbstractButton extends Button {

    Player player;

    public AbstractButton(Player player) {
        this.player = player;
    }

    public String getName(Player player) {
        return null;
    }

    public List<String> getLines(Player player) {
        return null;
    }

    public Material getMaterial(Player player) {
        return null;
    }

    public int getAmount(Player player) {
        return 0;
    }

    public HashMap<ClickType, List<Consumer<Player>>> getClickActions() {
        return null;
    }

    @Override
    public ItemStack build() {
        ItemStack item = new ItemStack(getMaterial(player), getAmount(player));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(getName(player));
            meta.setLore(getLines(player));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(ClickType clickType, Player player) {
        List<Consumer<Player>> actions = getClickActions().get(clickType);
        if (actions != null) {
            for (Consumer<Player> action : actions) {
                action.accept(player);
            }
        }
    }

}
