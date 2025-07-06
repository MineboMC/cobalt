package net.minebo.cobalt.menu.construct;

import lombok.AllArgsConstructor;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Button {

    public Supplier<String> name;
    private Supplier<List<String>> lines;
    public Supplier<Material> material;
    public Supplier<Integer> amount;
    private HashMap<ClickType, List<Consumer<Player>>> clickActions;

    public Button() {
        this.name = () -> "";
        this.lines = Arrays::asList;
        this.material = () -> Material.BOOK;
        this.amount = () -> 1;
        clickActions = new HashMap<>();
    }

    public Button setName(String name) {
        this.name = () -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(name));
        return this;
    }

    public Button setLines(String... dynamicLines) {
        this.lines = () -> Arrays.asList(dynamicLines).stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(line)))
                .collect(Collectors.toList());
        return this;
    }

    public Button setMaterial(Material material) {
        this.material = () -> material;
        return this;
    }

    public Button setAmount(Integer amount) {
        this.amount = () -> amount;
        return this;
    }

    public Button addClickAction(ClickType clickType, Consumer<Player> clickAction) {
        clickActions.computeIfAbsent(clickType, k -> new ArrayList<>()).add(clickAction);
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material.get());

        item.setAmount(amount.get());
        item.setLore(lines.get().stream().map(line -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(line))).collect(Collectors.toList()));

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name.get());
        item.setItemMeta(meta);

        return item;
    }

    public void onClick(ClickType clickType, Player player) {
        List<Consumer<Player>> actions = clickActions.get(clickType);
        if (actions != null) {
            for (Consumer<Player> action : actions) {
                action.accept(player);
            }
        }
    }

}