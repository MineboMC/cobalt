package net.minebo.cobalt.menu.impl.menu;

import lombok.Getter;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public abstract class PaginatedMenu extends Menu {

    @Getter
    private final Player player;
    private final int displaySize;

    @Getter
    private int currentPage = 0;
    @Getter
    private int maxPages = 0;

    public PaginatedMenu(int displaySize, Player player) {
        this.displaySize = displaySize;
        this.player = player;
    }

    public abstract Map<Integer, Button> getPagesButtons(Player player);

    public abstract String getTitle(Player player);

    public final Map<Integer, Button> getButtonsInRange(Player player) {
        Map<Integer, Button> buttonsInRange = new HashMap<>();
        Map<Integer, Button> allButtons = getPagesButtons(player);

        if (allButtons.isEmpty()) return buttonsInRange;

        List<Integer> positions = getButtonPositions();
        int buttonsPerPage = getButtonsPerPage();

        maxPages = (int) Math.ceil((double) allButtons.size() / buttonsPerPage);

        if (currentPage >= maxPages) {
            currentPage = Math.max(0, maxPages - 1);
        }

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

    public final Button getPreviousPageButton() {
        boolean canGoBack = currentPage > 0;
        if (canGoBack) {
            return new Button()
                    .setName("<green>⬅")
                    .setLines(
                            "<gray>Click to go to previous page",
                            "",
                            "<yellow>(" + (currentPage + 1) + "/" + maxPages + ")"
                    )
                    .setMaterial(Material.LIGHT_BLUE_CARPET)
                    .addClickAction(ClickType.LEFT, p -> {
                        if (currentPage > 0) {
                            currentPage--;
                            openMenu(player);
                        }
                    });
        } else {
            return new Button()
                    .setName("<gray>First Page")
                    .setLines("<red>You are on the first page")
                    .setMaterial(Material.GRAY_CARPET)
                    .addClickAction(ClickType.LEFT, p -> {});
        }
    }

    public final Button getNextPageButton() {
        boolean canGoForward = currentPage < maxPages - 1;
        if (canGoForward) {
            return new Button()
                    .setName("<green>➔")
                    .setLines(
                            "<gray>Click to go to next page",
                            "",
                            "<yellow>(" + (currentPage + 2) + "/" + maxPages + ")"
                    )
                    .setMaterial(Material.LIGHT_BLUE_CARPET)
                    .addClickAction(ClickType.LEFT, p -> {
                        if (currentPage < maxPages - 1) {
                            currentPage++;
                            openMenu(player);
                        }
                    });
        } else {
            return new Button()
                    .setName("<gray>Last Page")
                    .setLines("<red>You are on the last page")
                    .setMaterial(Material.GRAY_CARPET)
                    .addClickAction(ClickType.LEFT, p -> {});
        }
    }

    public Pair<Integer, Integer> getPageButtonPositions() {
        int lastRow = (displaySize / 9) - 1;
        return new Pair<>(lastRow * 9, lastRow * 9 + 8);
    }

    public int getButtonsStartAt() {
        return 9;
    }

    public int getButtonsPerPage() {
        int totalSlots = displaySize;
        int headerSlots = 9;
        int footerSlots = 9;
        return totalSlots - headerSlots - footerSlots;
    }

    public Map<Integer, Button> getHeaderItems(Player player) {
        return new HashMap<>();
    }

    public Material getPlaceholderMaterial() {
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    public boolean hidePlaceholderName() {
        return true;
    }

    public final void openMenu(Player player) {
        Menu menu = new Menu()
                .setTitle(getTitle(player))
                .setSize(displaySize)
                .setAutoUpdate(true)
                .setUpdateAfterClick(true);

        // Header
        Map<Integer, Button> headerItems = getHeaderItems(player);
        headerItems.forEach(menu::setButton);

        // Page content
        getButtonsInRange(player).forEach(menu::setButton);

        // Navigation
        if (maxPages > 1) {
            Pair<Integer, Integer> pos = getPageButtonPositions();
            if (currentPage > 0) {
                menu.setButton(pos.getFirst(), getPreviousPageButton());
            }
            if (currentPage < maxPages - 1) {
                menu.setButton(pos.getSecond(), getNextPageButton());
            }
        }

        // Page info
        if (maxPages > 1) {
            menu.setButton(displaySize / 2, createPageInfoButton());
        }

        menu.fillEmpty(getPlaceholderMaterial(), hidePlaceholderName());
        menu.openMenu(player);
    }

    private Button createPageInfoButton() {
        return new Button()
                .setName("<gold>Page " + (currentPage + 1) + "/" + maxPages)
                .setLines(
                        "",
                        "<gray>You are viewing page <yellow>" + (currentPage + 1),
                        "<gray>out of <yellow>" + maxPages + "<gray> total pages"
                )
                .setMaterial(Material.PAPER)
                .setAmount(Math.min(64, currentPage + 1));
    }

    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, maxPages - 1));
    }

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