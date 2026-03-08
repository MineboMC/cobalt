package net.minebo.cobalt.menu.impl.menu;

import lombok.Getter;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public abstract class PaginatedMenu {

    /**
     * -- GETTER --
     *  Get the player viewing this menu
     *
     * @return The player
     */
    @Getter
    private final Player player;
    private final int displaySize;
    /**
     * -- GETTER --
     *  Get the current page number
     *
     * @return Current page (0-indexed)
     */
    @Getter
    private int currentPage = 0;
    /**
     * -- GETTER --
     *  Get the max number of pages
     *
     * @return Max pages
     */
    @Getter
    private int maxPages = 0;

    public PaginatedMenu(int displaySize, Player player) {
        this.displaySize = displaySize;
        this.player = player;
    }

    /**
     * Get all buttons that should be paginated
     * @param player The player viewing the menu
     * @return Map of index -> Button for all page content
     */
    public abstract Map<Integer, Button> getPagesButtons(Player player);

    /**
     * Get the title of the menu
     * @param player The player viewing the menu
     * @return The menu title
     */
    public abstract String getTitle(Player player);

    /**
     * Get buttons for the current page range
     * @param player The player viewing the menu
     * @return Map of slot -> Button for current page
     */
    public final Map<Integer, Button> getButtonsInRange(Player player) {
        Map<Integer, Button> buttonsInRange = new HashMap<>();
        Map<Integer, Button> allButtons = getPagesButtons(player);
        
        if (allButtons.isEmpty()) {
            return buttonsInRange;
        }

        List<Integer> positions = getButtonPositions();
        int buttonsPerPage = getButtonsPerPage();
        
        // Calculate max pages
        maxPages = (int) Math.ceil((double) allButtons.size() / buttonsPerPage);
        
        // Ensure current page is valid
        if (currentPage >= maxPages) {
            currentPage = Math.max(0, maxPages - 1);
        }
        
        // Get buttons for current page
        List<Map.Entry<Integer, Button>> buttonList = new ArrayList<>(allButtons.entrySet());
        int startIndex = currentPage * buttonsPerPage;
        int endIndex = Math.min(startIndex + buttonsPerPage, buttonList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int positionIndex = i - startIndex;
            if (positionIndex < positions.size()) {
                buttonsInRange.put(positions.get(positionIndex), buttonList.get(i).getValue());
            }
        }
        
        return buttonsInRange;
    }

    /**
     * Get the positions where page buttons should be placed
     * @return List of slot positions
     */
    public List<Integer> getButtonPositions() {
        List<Integer> positions = new ArrayList<>();
        int start = getButtonsStartAt();
        int perPage = getButtonsPerPage();
        
        for (int i = 0; i < perPage; i++) {
            int row = i / 9;
            int col = i % 9;
            positions.add((row + (start / 9)) * 9 + col);
        }
        
        return positions;
    }

    /**
     * Get the previous page button
     * @return Button for previous page
     */
    public final Button getPreviousPageButton() {
        boolean canGoBack = currentPage > 0;
        
        return new Button()
                .setName(canGoBack ? ChatColor.YELLOW + "« Previous Page" : ChatColor.GRAY + "« Previous Page")
                .setLines(
                        "",
                        canGoBack 
                            ? ChatColor.GRAY + "Click to go to page " + ChatColor.YELLOW + currentPage
                            : ChatColor.RED + "You are on the first page"
                )
                .setMaterial(canGoBack ? Material.ARROW : Material.ARROW)
                .addClickAction(ClickType.LEFT, p -> {
                    if (currentPage > 0) {
                        currentPage--;
                        openMenu(player);
                    }
                })
                .addClickAction(ClickType.RIGHT, p -> {
                    if (currentPage > 0) {
                        currentPage--;
                        openMenu(player);
                    }
                });
    }

    /**
     * Get the next page button
     * @return Button for next page
     */
    public final Button getNextPageButton() {
        boolean canGoForward = currentPage < maxPages - 1;
        
        return new Button()
                .setName(canGoForward ? ChatColor.YELLOW + "Next Page »" : ChatColor.GRAY + "Next Page »")
                .setLines(
                        "",
                        canGoForward 
                            ? ChatColor.GRAY + "Click to go to page " + ChatColor.YELLOW + (currentPage + 2)
                            : ChatColor.RED + "You are on the last page"
                )
                .setMaterial(canGoForward ? Material.ARROW : Material.ARROW)
                .addClickAction(ClickType.LEFT, p -> {
                    if (currentPage < maxPages - 1) {
                        currentPage++;
                        openMenu(player);
                    }
                })
                .addClickAction(ClickType.RIGHT, p -> {
                    if (currentPage < maxPages - 1) {
                        currentPage++;
                        openMenu(player);
                    }
                });
    }

    /**
     * Get the positions for previous and next page buttons
     * @return Pair of (previousSlot, nextSlot)
     */
    public Pair<Integer, Integer> getPageButtonPositions() {
        // Default: bottom left and bottom right corners of last row
        int lastRow = (displaySize / 9) - 1;
        return new Pair<>(lastRow * 9, lastRow * 9 + 8);
    }

    /**
     * Get the starting slot for page content buttons
     * @return Starting slot
     */
    public int getButtonsStartAt() {
        return 9; // Skip first row by default
    }

    /**
     * Get the number of buttons per page
     * @return Buttons per page
     */
    public int getButtonsPerPage() {
        // Default: all slots except first row (header) and last row (navigation)
        int totalSlots = displaySize;
        int headerSlots = 9;
        int footerSlots = 9;
        return totalSlots - headerSlots - footerSlots;
    }

    /**
     * Get static header buttons that appear on every page
     * @param player The player viewing the menu
     * @return Map of slot -> Button for header
     */
    public Map<Integer, Button> getHeaderItems(Player player) {
        return new HashMap<>();
    }

    /**
     * Get the material to fill empty slots with
     * @return Material for empty slots
     */
    public Material getPlaceholderMaterial() {
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Whether to hide the name of placeholder items
     * @return true to hide placeholder names
     */
    public boolean hidePlaceholderName() {
        return true;
    }

    /**
     * Open the paginated menu for the player
     */
    public final void openMenu(Player player) {
        Menu menu = new Menu()
                .setTitle(getTitle(player))
                .setSize(displaySize)
                .setUpdateAfterClick(false);

        // Add header items
        Map<Integer, Button> headerItems = getHeaderItems(player);
        for (Map.Entry<Integer, Button> entry : headerItems.entrySet()) {
            menu.setButton(entry.getKey(), entry.getValue());
        }

        // Add page content buttons
        Map<Integer, Button> buttonsInRange = getButtonsInRange(player);
        for (Map.Entry<Integer, Button> entry : buttonsInRange.entrySet()) {
            menu.setButton(entry.getKey(), entry.getValue());
        }

        // Add navigation buttons if there are multiple pages
        if (maxPages > 1) {
            Pair<Integer, Integer> pageButtonPos = getPageButtonPositions();
            
            if (currentPage > 0) {
                menu.setButton(pageButtonPos.getFirst(), getPreviousPageButton());
            }
            
            if (currentPage < maxPages - 1) {
                menu.setButton(pageButtonPos.getSecond(), getNextPageButton());
            }
        }

        // Add page info button (center of footer)
        if (maxPages > 1) {
            menu.setButton(displaySize / 2, createPageInfoButton());
        }

        // Fill empty slots
        menu.fillEmpty(getPlaceholderMaterial(), hidePlaceholderName());

        // Open the menu
        menu.openMenu(player);
    }

    /**
     * Create a button showing current page info
     * @return Page info button
     */
    private Button createPageInfoButton() {
        return new Button()
                .setName(ChatColor.GOLD + "Page " + (currentPage + 1) + "/" + maxPages)
                .setLines(
                        "",
                        ChatColor.GRAY + "You are viewing page " + ChatColor.YELLOW + (currentPage + 1),
                        ChatColor.GRAY + "out of " + ChatColor.YELLOW + maxPages + ChatColor.GRAY + " total pages"
                )
                .setMaterial(Material.PAPER)
                .setAmount(Math.min(64, currentPage + 1));
    }

    /**
     * Set the current page number
     * @param page Page number (0-indexed)
     */
    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, maxPages - 1));
    }

    /**
     * Simple Pair class for storing two values
     */
    public static class Pair<F, S> {
        private final F first;
        private final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}