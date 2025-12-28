package net.okocraft.box.feature.gui.internal.menu;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.CloseButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.menu.paginate.PaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.internal.button.CategoryButton;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategorySelectorMenu extends AbstractPaginatedMenu<Category> {

    private static final TypedKey<Integer> CURRENT_PAGE_KEY = PaginatedMenu.createCurrentPageKey("category_selector");
    private static final List<Button> FOOTER;
    private static final MessageKey.Arg1<StockHolder> TITLE = MessageKey.arg1(DisplayKeys.CATEGORY_SELECTOR_MENU_TITLE, holder -> Argument.string("name", holder.getName()));

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
        super(6, CategoryRegistry.get().values(), CURRENT_PAGE_KEY);
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(session.getSourceStockHolder()).asComponent();
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
