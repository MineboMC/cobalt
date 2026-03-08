package net.minebo.cobalt.menu.impl.button;

import com.google.common.base.Preconditions;
import net.minebo.cobalt.menu.construct.Button;
import org.bukkit.ChatColor;
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

    /**
     * Create a new integer trait button with default bounds (0-100) and increment (1)
     */
    public IntegerTraitButton(T object, String traitName, BiConsumer<T, Integer> setter, Function<T, Integer> getter, Consumer<T> saveCallback) {
        this(object, traitName, setter, getter, saveCallback, 0, 100, 1, 0);
    }

    /**
     * Create a new integer trait button with custom bounds and increments
     */
    public IntegerTraitButton(T object, String traitName, BiConsumer<T, Integer> setter, Function<T, Integer> getter, Consumer<T> saveCallback, int minValue, int maxValue, int incrementAmount, int shiftIncrementAmount) {
        this.object = Preconditions.checkNotNull(object, "object");
        this.traitName = Preconditions.checkNotNull(traitName, "traitName");
        this.setter = Preconditions.checkNotNull(setter, "setter");
        this.getter = Preconditions.checkNotNull(getter, "getter");
        this.saveCallback = Preconditions.checkNotNull(saveCallback, "saveCallback");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.incrementAmount = incrementAmount;
        this.shiftIncrementAmount = shiftIncrementAmount;

        // Normal clicks = small increment
        addClickAction(ClickType.LEFT, p -> handleClick(p, true, incrementAmount));
        addClickAction(ClickType.RIGHT, p -> handleClick(p, false, incrementAmount));
        
        // Shift clicks = large increment
        if (shiftIncrementAmount > 0) {
            addClickAction(ClickType.SHIFT_LEFT, p -> handleClick(p, true, shiftIncrementAmount));
            addClickAction(ClickType.SHIFT_RIGHT, p -> handleClick(p, false, shiftIncrementAmount));
        }
    }

    @Override
    public ItemStack build() {
        int currentValue = getter.apply(object);
        
        ItemStack item = new ItemStack(Material.PAPER);
        item.setAmount(Math.min(64, Math.max(1, currentValue))); // Visual indicator
        
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD + traitName);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.WHITE + currentValue);
            lore.add("");
            lore.add(ChatColor.GREEN + "▲ Left Click: " + ChatColor.GRAY + "+" + incrementAmount);
            if (shiftIncrementAmount > 0) lore.add(ChatColor.GREEN + "▲ Shift + Left Click: " + ChatColor.GRAY + "+" + shiftIncrementAmount);
            lore.add(ChatColor.RED + "▼ Right Click: " + ChatColor.GRAY + "-" + incrementAmount);
            if (shiftIncrementAmount > 0) lore.add(ChatColor.RED + "▼ Shift + Right Click: " + ChatColor.GRAY + "-" + shiftIncrementAmount);
            lore.add("");
            lore.add(ChatColor.GRAY + "Range: " + ChatColor.YELLOW + minValue + ChatColor.GRAY + " - " + ChatColor.YELLOW + maxValue);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void handleClick(Player player, boolean increment, int amount) {
        int currentValue = getter.apply(object);
        int newValue;
        
        if (increment) {
            newValue = Math.min(maxValue, currentValue + amount);
        } else {
            newValue = Math.max(minValue, currentValue - amount);
        }
        
        // Check if value actually changed
        if (newValue == currentValue) {
            String limit = increment ? "maximum" : "minimum";
            player.sendMessage(ChatColor.RED + "Already at " + limit + " value (" + currentValue + ")");
            return;
        }
        
        // Update value
        setter.accept(object, newValue);
        saveCallback.accept(object);
        
        // Send feedback
        String direction = increment ? ChatColor.GREEN + "increased" : ChatColor.RED + "decreased";
        player.sendMessage(ChatColor.YELLOW + traitName + " has been " + direction + ChatColor.YELLOW + " to " + ChatColor.WHITE + newValue + ChatColor.YELLOW + ".");
    }
}