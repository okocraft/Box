package net.okocraft.box.feature.gui.api.menu.paginate;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
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
    private final TypedKey<Integer> currentPageKey;

    private static final SoundBase PAGE_CHANGE_SOUND = SoundBase.builder().sound(Sound.BLOCK_LEVER_CLICK).pitch(1.5f).build();

    private final int iconsPerPage;
    private final int maxPage;

    protected AbstractPaginatedMenu(int rows, @NotNull List<T> list, @NotNull TypedKey<Integer> currentPageKey) {
        this.rows = rows;
        this.list = list;
        this.currentPageKey = currentPageKey;
        this.iconsPerPage = (this.getRows() - 1) * 9;
        this.maxPage = (list.size() + this.iconsPerPage - 1) / this.iconsPerPage;
    }

    @Override
    public final int getRows() {
        return this.rows;
    }

    @Override
    public final int getMaxPage() {
        return this.maxPage;
    }

    @Override
    public final int getIconsPerPage() {
        return this.iconsPerPage;
    }

    @Override
    public int getCurrentPage(@NotNull PlayerSession session) {
        Integer data = session.getData(this.currentPageKey);
        return data != null ? data : 1;
    }

    @Override
    public void setCurrentPage(@NotNull PlayerSession session, int page) {
        session.putData(this.currentPageKey, page);
    }

    @Override
    public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
        var buttons = new ArrayList<Button>();
        int currentPage = this.getCurrentPage(session);

        int start = (currentPage - 1) * this.iconsPerPage;
        int end = start + this.iconsPerPage;

        for (int i = start, limit = this.list.size(), slot = 0; i < limit && i < end; i++, slot++) {
            buttons.add(this.createButton(this.list.get(i), slot));
        }

        if (currentPage < this.maxPage) {
            buttons.add(new PageSwitchButton(this.rows, currentPage + 1, true, this.currentPageKey));
        }

        if (1 < currentPage) {
            buttons.add(new PageSwitchButton(this.rows, currentPage - 1, false, this.currentPageKey));
        }

        this.addAdditionalButtons(session, buttons);

        return buttons;
    }

    protected abstract @NotNull Button createButton(@NotNull T instance, int slot);

    protected abstract void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons);

    private record PageSwitchButton(int rows, int newPage, boolean next,
                                    @NotNull TypedKey<Integer> currentPageKey) implements Button {

        private static final MiniMessageBase PREVIOUS_PAGE = MiniMessageBase.messageKey(DisplayKeys.PREVIOUS_PAGE);
        private static final MiniMessageBase NEXT_PAGE = MiniMessageBase.messageKey(DisplayKeys.NEXT_PAGE);

        @Override
        public int getSlot() {
            return this.next ? this.rows * 9 - 1 : (this.rows - 1) * 9;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            return ItemEditor.create()
                    .displayName((this.next ? NEXT_PAGE : PREVIOUS_PAGE).create(session.getMessageSource()))
                    .createItem(Material.ARROW, Math.min(this.newPage, 64));
        }

        @Override
        public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            session.putData(this.currentPageKey, this.newPage);
            PAGE_CHANGE_SOUND.play(session.getViewer());
            return ClickResult.UPDATE_ICONS;
        }
    }
}
