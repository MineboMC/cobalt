package net.minebo.cobalt.menu.impl.button;

import com.google.common.base.Preconditions;
import net.minebo.cobalt.menu.construct.Button;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A button that toggles a boolean trait on an object
 * @param <T> The type of object this button modifies
 */
public class BooleanTraitButton<T> extends Button {

    private final T object;
    private final String traitName;
    private final BiConsumer<T, Boolean> setter;
    private final Function<T, Boolean> getter;
    private final Consumer<T> saveCallback;

    /**
     * Create a new boolean trait button
     * @param object The object to modify
     * @param traitName The display name of the trait (e.g., "Hidden", "Enabled")
     * @param setter Method to set the boolean value (e.g., KitType::setHidden)
     * @param getter Method to get the current boolean value (e.g., KitType::isHidden)
     * @param saveCallback Callback to save the object after modification (e.g., KitType::saveAsync)
     */
    public BooleanTraitButton(T object, String traitName, BiConsumer<T, Boolean> setter, Function<T, Boolean> getter, Consumer<T> saveCallback) {
        this.object = Preconditions.checkNotNull(object, "object");
        this.traitName = Preconditions.checkNotNull(traitName, "traitName");
        this.setter = Preconditions.checkNotNull(setter, "setter");
        this.getter = Preconditions.checkNotNull(getter, "getter");
        this.saveCallback = Preconditions.checkNotNull(saveCallback, "saveCallback");

        addClickAction(ClickType.LEFT, this::handleClick);
        addClickAction(ClickType.RIGHT, this::handleClick);
    }

    @Override
    public org.bukkit.inventory.ItemStack build() {
        boolean currentValue = getter.apply(object);

        // Use wool with data value for 1.12
        org.bukkit.inventory.ItemStack item = currentValue ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            ChatColor nameColor = currentValue ? ChatColor.GREEN : ChatColor.RED;
            meta.setDisplayName(nameColor.toString() + ChatColor.BOLD + traitName);

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Status: " + (currentValue ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to " + (currentValue ? "disable" : "enable"));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void handleClick(Player player) {
        boolean currentValue = getter.apply(object);
        boolean newValue = !currentValue;
        setter.accept(object, newValue);  // Changed: now passes both object and value
        saveCallback.accept(object);

        String status = newValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled";
        player.sendMessage(ChatColor.YELLOW + traitName + " has been " + status + ChatColor.YELLOW + ".");
    }
}