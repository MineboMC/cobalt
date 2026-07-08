package net.minebo.cobalt.menu.construct;

import lombok.AllArgsConstructor;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@AllArgsConstructor
public class Button {

    public Supplier<String> name;
    public Supplier<List<String>> lines;
    public Supplier<Material> material;
    public Supplier<Integer> amount;
    private HashMap<ClickType, List<Consumer<Player>>> clickActions;

    // Special Cases
    public Supplier<PotionType> potionType;
    public Supplier<UUID> skullOwner;

    public Button() {
        this.name = () -> "";
        this.lines = ArrayList::new;
        this.material = () -> Material.BOOK;
        this.amount = () -> 1;
        this.clickActions = new HashMap<>();

        this.potionType = () -> null;
        this.skullOwner = () -> null;        // ← NEW
    }

    public Button setName(String name) {
        this.name = () -> ColorUtil.translateColors((name));
        return this;
    }

    public Button setName(Supplier<String> nameSupplier) {
        this.name = () -> ColorUtil.translateColors((nameSupplier.get()));
        return this;
    }

    public Button setLines(String... dynamicLines) {
        this.lines = () -> Arrays.stream(dynamicLines)
                .map(ColorUtil::translateColors)
                .toList();
        return this;
    }

    public Button setLines(Supplier<List<String>> linesSupplier) {
        this.lines = () -> linesSupplier.get().stream()
                .map(ColorUtil::translateColors)
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

    public Button setSkullOwner(UUID uuid) {
        this.skullOwner = () -> uuid;
        return this;
    }

    public Button setSkullOwner(Supplier<UUID> skullOwnerSupplier) {
        this.skullOwner = skullOwnerSupplier;
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

    public Boolean shouldCancel(Player player, ClickType clickType) {
        return true;
    }

    public ItemStack build() {
        Material mat = material.get();
        ItemStack item = new ItemStack(mat, (amount.get() > 0) ? amount.get() : 1);
        ItemMeta meta = item.getItemMeta();

        // Potion support
        if (meta instanceof PotionMeta potionMeta) {
            PotionType type = this.potionType.get();
            if (type != null) {
                potionMeta.setBasePotionType(type);
            }
        }

        // Skull support
        if (meta instanceof SkullMeta skullMeta && mat == Material.PLAYER_HEAD) {
            UUID owner = this.skullOwner.get();
            if (owner != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            }
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