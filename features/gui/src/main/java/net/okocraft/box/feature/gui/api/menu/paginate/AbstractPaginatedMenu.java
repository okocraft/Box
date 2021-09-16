package net.okocraft.box.feature.gui.api.menu.paginate;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.AbstractMenu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPaginatedMenu<T> extends AbstractMenu implements PaginatedMenu {

    private final int iconsPerPage;
    private final int maxPage;

    private final List<T> list;
    private int currentPage = 1;

    private boolean moved = true;

    protected AbstractPaginatedMenu(@NotNull List<T> list) {
        this.list = list;

        int page = 1;
        int size = list.size();
        this.iconsPerPage = (getRows() - 1) * 9;

        while (page * iconsPerPage < size) {
            page++;
        }

        this.maxPage = page;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public boolean hasNext() {
        return currentPage < maxPage;
    }

    @Override
    public boolean hasPrevious() {
        return 1 < currentPage;
    }

    @Override
    public void previous() {
        if (!hasPrevious()) {
            throw new IllegalStateException();
        }

        currentPage--;
        moved = true;
    }

    @Override
    public void next() {
        if (!hasNext()) {
            throw new IllegalStateException();
        }

        currentPage++;
        moved = true;
    }

    @Override
    public void updateMenu(@NotNull Player viewer) {
        renderPage(viewer);
        moved = false;
    }

    @Override
    public boolean shouldUpdate() {
        return moved;
    }

    private void renderPage(@NotNull Player viewer) {
        int start = (currentPage - 1) * iconsPerPage;
        int end = start + iconsPerPage;

        var newButtons = new ArrayList<Button>();

        for (int i = start, limit = list.size(), slot = 0; i < limit && i < end; i++, slot++) {
            newButtons.add(createButton(list.get(i), slot));
        }

        if (hasNext()) {
            newButtons.add(new PageSwitchButton(true));
        }

        if (hasPrevious()) {
            newButtons.add(new PageSwitchButton(false));
        }

        addAdditionalButtons(viewer, newButtons);

        buttonMap.clear();

        for (var button : newButtons) {
            buttonMap.put(button.getSlot(), new RenderedButton(button));
        }

        buttonMap.values().forEach(button -> button.updateIcon(viewer));

        updated = true;
    }

    protected abstract @NotNull Button createButton(@NotNull T instance, int slot);

    protected abstract void addAdditionalButtons(@NotNull Player viewer, @NotNull List<Button> buttons);

    private class PageSwitchButton implements Button {

        private final boolean next;

        private PageSwitchButton(boolean next) {
            this.next = next;
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return Material.ARROW;
        }

        @Override
        public int getIconAmount() {
            return currentPage + (next ? 1 : -1);
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            var name = next ? Displays.PAGE_SWITCH_BUTTON_NEXT : Displays.PAGE_SWITCH_BUTTON_PREVIOUS;

            target.displayName(TranslationUtil.render(name, viewer));

            return target;
        }

        @Override
        public int getSlot() {
            return next ? getRows() * 9 - 1 : (getRows() - 1) * 9;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            if (next) {
                if (hasNext()) {
                    next();
                }
            } else {
                if (hasPrevious()) {
                    previous();
                }
            }

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_LEVER_CLICK, 100f, 1.5f);
        }
    }
}
