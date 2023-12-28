package net.okocraft.box.feature.gui.internal.menu;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.CloseButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.button.CategoryButton;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategorySelectorMenu extends AbstractPaginatedMenu<Category> {

    private static final List<Button> FOOTER;
    private static final Arg1<StockHolder> TITLE = Arg1.arg1(DisplayKeys.CATEGORY_SELECTOR_MENU_TITLE, Placeholder.component("name", holder -> Component.text(holder.getName())));

    static {
        var footer = new ArrayList<Button>(9);

        footer.add(new CloseButton(49));

        for (int slot = 45; slot < 54; slot++) {
            if (slot != 49) {
                footer.add(Button.glassPane(slot));
            }
        }

        FOOTER = Collections.unmodifiableList(footer);
    }

    public CategorySelectorMenu() {
        super(6, CategoryRegistry.get().values());
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(session.getStockHolder()).create(session.getMessageSource());
    }

    @Override
    protected @NotNull Button createButton(@NotNull Category instance, int slot) {
        return new CategoryButton(instance, slot);
    }

    @Override
    protected void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons) {
        buttons.addAll(FOOTER);
    }
}
