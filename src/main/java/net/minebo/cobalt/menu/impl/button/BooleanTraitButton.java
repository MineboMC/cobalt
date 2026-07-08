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
 * A button that toggles a boolean trait on an object
 * @param <T> The type of object this button modifies
 */
public class BooleanTraitButton<T> extends Button {

    private final T object;
    private final String traitName;
    private final BiConsumer<T, Boolean> setter;
    private final Function<T, Boolean> getter;
    private final Consumer<T> saveCallback;

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
    public ItemStack build() {
        boolean currentValue = getter.apply(object);

        ItemStack item = currentValue
                ? new ItemStack(Material.GREEN_WOOL)
                : new ItemStack(Material.RED_WOOL);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String statusColor = currentValue ? "<green>" : "<red>";

            meta.setDisplayName(ColorUtil.translateColors(statusColor + "<bold>" + traitName));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.translateColors("<gray>Status: " + (currentValue ? "<green>Enabled" : "<red>Disabled")));
            lore.add("");
            lore.add(ColorUtil.translateColors("<yellow>Click to " + (currentValue ? "disable" : "enable")));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void handleClick(Player player) {
        boolean currentValue = getter.apply(object);
        boolean newValue = !currentValue;

        setter.accept(object, newValue);
        saveCallback.accept(object);

        String status = newValue ? "<green>enabled" : "<red>disabled";
        player.sendMessage(ColorUtil.translateColors("<yellow>" + traitName + " has been " + status + "<yellow>."));
    }
}