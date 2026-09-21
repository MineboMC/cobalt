package net.minebo.cobalt.service.tablist;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.util.Coloring;

public final class TabLayout {

    public static final int MAX_COLUMNS = 4;
    public static final int ROWS = 20;
    public static final int MAX_SLOTS = MAX_COLUMNS * ROWS;

    public static final int COLUMNS = MAX_COLUMNS;
    public static final int SLOT_COUNT = MAX_SLOTS;

    private final int columns;
    private final TabSlot[] slots;

    public TabLayout() {
        this(MAX_COLUMNS);
    }

    public TabLayout(int columns) {
        this.columns = Math.max(1, Math.min(MAX_COLUMNS, columns));
        this.slots = new TabSlot[slotCount()];
        for (int index = 0; index < slots.length; index++) {
            this.slots[index] = TabSlot.EMPTY;
        }
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return ROWS;
    }

    public int slotCount() {
        return columns * ROWS;
    }

    public TabLayout set(int column, int row, TabSlot slot) {
        if (!valid(column, row)) {
            return this;
        }
        slots[index(column, row)] = slot == null ? TabSlot.EMPTY : slot;
        return this;
    }

    public TabLayout set(int column, int row, Component text) {
        return set(column, row, TabSlot.of(text));
    }

    public TabLayout set(int column, int row, Component text, TabSkin skin) {
        return set(column, row, TabSlot.of(text, skin));
    }

    /**
     * Supports legacy (&) codes and MiniMessage tags in the same string.
     */
    public TabLayout set(int column, int row, String text) {
        return set(column, row, Coloring.toComponent(text));
    }

    public TabSlot get(int column, int row) {
        if (!valid(column, row)) {
            return TabSlot.EMPTY;
        }
        return slots[index(column, row)];
    }

    public TabSlot slot(int index) {
        if (index < 0 || index >= slots.length) {
            return TabSlot.EMPTY;
        }
        return slots[index];
    }

    public int index(int column, int row) {
        return column * ROWS + row;
    }

    public static int listOrder(int index) {
        int column = index / ROWS;
        int row = index % ROWS;
        return column * ROWS + (ROWS - 1 - row);
    }

    public boolean valid(int column, int row) {
        return column >= 0 && column < columns && row >= 0 && row < ROWS;
    }
}