package net.minebo.cobalt.menu.impl.button;

import com.google.common.base.Preconditions;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A button that modifies an integer trait on an object
 * @param <T> The type of object this button modifies
 */
public class IntegerTraitButton<T> extends Button {

    private final T object;
    private final String traitName;
    private final BiConsumer<T, Integer> setter;
    private final Function<T, Integer> getter;
    private final Consumer<T> saveCallback;
    private final int minValue;
    private final int maxValue;
    private final int incrementAmount;
    private final int shiftIncrementAmount;

    public IntegerTraitButton(T object, String traitName, BiConsumer<T, Integer> setter, Function<T, Integer> getter, Consumer<T> saveCallback) {
        this(object, traitName, setter, getter, saveCallback, 0, 100, 1, 0);
    }

    public IntegerTraitButton(T object, String traitName, BiConsumer<T, Integer> setter, Function<T, Integer> getter, Consumer<T> saveCallback,
                              int minValue, int maxValue, int incrementAmount, int shiftIncrementAmount) {
        this.object = Preconditions.checkNotNull(object, "object");
        this.traitName = Preconditions.checkNotNull(traitName, "traitName");
        this.setter = Preconditions.checkNotNull(setter, "setter");
        this.getter = Preconditions.checkNotNull(getter, "getter");
        this.saveCallback = Preconditions.checkNotNull(saveCallback, "saveCallback");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.incrementAmount = incrementAmount;
        this.shiftIncrementAmount = shiftIncrementAmount;

        addClickAction(ClickType.LEFT, p -> handleClick(p, true, incrementAmount));
        addClickAction(ClickType.RIGHT, p -> handleClick(p, false, incrementAmount));

        if (shiftIncrementAmount > 0) {
            addClickAction(ClickType.SHIFT_LEFT, p -> handleClick(p, true, shiftIncrementAmount));
            addClickAction(ClickType.SHIFT_RIGHT, p -> handleClick(p, false, shiftIncrementAmount));
        }
    }

    @Override
    public ItemStack build() {
        int currentValue = getter.apply(object);

        ItemStack item = new ItemStack(Material.PAPER);
        item.setAmount(Math.min(64, Math.max(1, currentValue)));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.translateColors("<yellow><bold>" + traitName));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.translateColors("<gray>Current Value: <white>" + currentValue));
            lore.add("");
            lore.add(ColorUtil.translateColors("<green>▲ Left Click: <gray>+" + incrementAmount));
            if (shiftIncrementAmount > 0) {
                lore.add(ColorUtil.translateColors("<green>▲ Shift + Left Click: <gray>+" + shiftIncrementAmount));
            }
            lore.add(ColorUtil.translateColors("<red>▼ Right Click: <gray>-" + incrementAmount));
            if (shiftIncrementAmount > 0) {
                lore.add(ColorUtil.translateColors("<red>▼ Shift + Right Click: <gray>-" + shiftIncrementAmount));
            }
            lore.add("");
            lore.add(ColorUtil.translateColors("<gray>Range: <yellow>" + minValue + "<gray> - <yellow>" + maxValue));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void handleClick(Player player, boolean increment, int amount) {
        int currentValue = getter.apply(object);
        int newValue = increment
                ? Math.min(maxValue, currentValue + amount)
                : Math.max(minValue, currentValue - amount);

        if (newValue == currentValue) {
            String limit = increment ? "maximum" : "minimum";
            player.sendMessage(ColorUtil.translateColors("<red>Already at " + limit + " value (" + currentValue + ")"));
            return;
        }

        setter.accept(object, newValue);
        saveCallback.accept(object);

        String direction = increment ? "<green>increased" : "<red>decreased";
        player.sendMessage(ColorUtil.translateColors("<yellow>" + traitName + " has been " + direction +
                "<yellow> to <white>" + newValue + "<yellow>."));
    }
}