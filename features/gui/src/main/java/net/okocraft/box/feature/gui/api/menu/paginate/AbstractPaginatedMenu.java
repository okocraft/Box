package net.okocraft.box.feature.gui.api.menu.paginate;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPaginatedMenu<T> implements PaginatedMenu {

    private final int rows;
    private final List<T> list;

    private static final SoundBase PAGE_CHANGE_SOUND = SoundBase.builder().sound(Sound.BLOCK_LEVER_CLICK).pitch(1.5f).build();

    private final int iconsPerPage;
    private final int maxPage;

    protected AbstractPaginatedMenu(int rows, @NotNull List<T> list) {
        this.rows = rows;
        this.list = list;
        this.iconsPerPage = (getRows() - 1) * 9;
        this.maxPage = (list.size() + iconsPerPage - 1) / iconsPerPage;
    }

    @Override
    public final int getRows() {
        return rows;
    }

    @Override
    public final int getMaxPage() {
        return maxPage;
    }

    @Override
    public final int getIconsPerPage() {
        return iconsPerPage;
    }

    @Override
    public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
        var buttons = new ArrayList<Button>();
        int currentPage = PaginatedMenu.getCurrentPage(session);

        int start = (currentPage - 1) * iconsPerPage;
        int end = start + iconsPerPage;

        for (int i = start, limit = list.size(), slot = 0; i < limit && i < end; i++, slot++) {
            buttons.add(createButton(list.get(i), slot));
        }

        if (currentPage < maxPage) {
            buttons.add(new PageSwitchButton(rows, maxPage, true));
        }

        if (1 < currentPage) {
            buttons.add(new PageSwitchButton(rows, maxPage, false));
        }

        addAdditionalButtons(session, buttons);

        return buttons;
    }

    protected abstract @NotNull Button createButton(@NotNull T instance, int slot);

    protected abstract void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons);

    protected record PageSwitchButton(int rows, int maxPage, boolean next) implements Button {

        @Override
        public int getSlot() {
            return next ? rows * 9 - 1 : (rows - 1) * 9;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            int amount = Math.min(PaginatedMenu.getCurrentPage(session) + (next ? 1 : -1), 64);

            var icon = new ItemStack(Material.ARROW, amount);

            var displayName = next ? Displays.PAGE_SWITCH_BUTTON_NEXT : Displays.PAGE_SWITCH_BUTTON_PREVIOUS;
            icon.editMeta(meta -> meta.displayName(TranslationUtil.render(displayName, session.getViewer())));

            return icon;
        }

        @Override
        public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            int currentPage = PaginatedMenu.getCurrentPage(session);
            int newPage = 0;

            if (next) {
                if (currentPage < maxPage) {
                    newPage = currentPage + 1;
                }
            } else {
                if (1 < currentPage) {
                    newPage = currentPage - 1;
                }
            }

            if (currentPage != newPage) {
                session.putData(PaginatedMenu.CURRENT_PAGE_KEY, newPage);
                PAGE_CHANGE_SOUND.play(session.getViewer());
                return ClickResult.UPDATE_ICONS;
            } else {
                return ClickResult.NO_UPDATE_NEEDED;
            }
        }
    }
}
