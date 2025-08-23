package net.minebo.cobalt.menu.construct;

import com.github.retrooper.packetevents.protocol.potion.Potion;
import lombok.AllArgsConstructor;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@AllArgsConstructor
public class Button {

    public Supplier<String> name;
    private Supplier<List<String>> lines;
    public Supplier<Material> material;
    public Supplier<Integer> amount;
    private HashMap<ClickType, List<Consumer<Player>>> clickActions;

    // Special Cases
    public Supplier<PotionType> potionType;

    public Button() {
        this.name = () -> "";
        this.lines = ArrayList::new;
        this.material = () -> Material.BOOK;
        this.amount = () -> 1;
        this.clickActions = new HashMap<>();
    }

    // --- Fluent setters ---
    public Button setName(String name) {
        this.name = () -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(name));
        return this;
    }
    public Button setName(Supplier<String> nameSupplier) {
        this.name = () -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(nameSupplier.get()));
        return this;
    }

    // For static lines
    public Button setLines(String... dynamicLines) {
        this.lines = () -> Arrays.stream(dynamicLines)
                .map(line -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(line)))
                .toList();
        return this;
    }

    // For dynamic lines
    public Button setLines(Supplier<List<String>> linesSupplier) {
        this.lines = () -> linesSupplier.get().stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', ColorUtil.translateHexColors(line)))
                .toList();
        return this;
    }

    public Button setMaterial(Material material) {
        this.material = () -> material;
        return this;
    }

    public Button setMaterial(Supplier<Material> materialSupplier) {
        this.material = materialSupplier;
        return this;
    }

    public Button setAmount(Integer amount) {
        this.amount = () -> amount;
        return this;
    }

    public Button setAmount(Supplier<Integer> amountSupplier) {
        this.amount = amountSupplier;
        return this;
    }

    public Button addClickAction(ClickType clickType, Consumer<Player> clickAction) {
        clickActions.computeIfAbsent(clickType, k -> new ArrayList<>()).add(clickAction);
        return this;
    }

    public Button setPotionType(PotionType potionType) {
        this.potionType = () -> potionType;
        return this;
    }

    public Button setPotionType(Supplier<PotionType> potionType) {
        this.potionType = potionType;
        return this;
    }


    // --- Main build method ---
    public ItemStack build() {
        ItemStack item = new ItemStack(material.get(), amount.get());
        ItemMeta meta = item.getItemMeta();

        if(meta instanceof PotionMeta) {
            ((PotionMeta) meta).setBasePotionType(potionType.get());
        }

        if (meta != null) {
            meta.setDisplayName(name.get());
            meta.setLore(lines.get());
            item.setItemMeta(meta);
        }
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