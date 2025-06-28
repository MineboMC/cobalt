package net.minebo.cobalt.menu.construct;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class Button {

    public String name;
    public List<String> lines;
    public Material material;
    public Integer amount;
    private Consumer<Player> clickAction;

    public Button() {
        this.name = "Default Title";
        this.lines = Arrays.asList("Default", "Lines");
        this.material = Material.BOOK;
        this.amount = 1;
    }

    public Button setName(String name) {
        this.name = name;
        return this;
    }

    public Button setLines(String... lines) {
        this.lines = Arrays.asList(lines);
        return this;
    }

    public Button setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public Button setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public Button setClickAction(Consumer<Player> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material);

        item.setAmount(amount);
        item.setLore(lines);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public void onClick(Player player) {
        if (clickAction != null) {
            clickAction.accept(player);
        }
    }

}