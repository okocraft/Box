package net.okocraft.box.feature.gui.internal.menu;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.buttons.amount.DecreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.IncreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.UnitChangeButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.button.BoxItemButton;
import net.okocraft.box.feature.gui.internal.button.ModeButton;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class CategoryMenu extends AbstractPaginatedMenu<BoxItem> {

    private static final List<Button> SHARED_BUTTONS;
    private static final Arg1<Component> TITLE;

    static {
        SHARED_BUTTONS = List.of(
                new DecreaseAmountButton(
                        46,
                        Amount.SHARED_DATA_KEY,
                        messageKey(DisplayKeys.DECREASE_TRANSACTION_AMOUNT_DISPLAY_NAME),
                        arg1(DisplayKeys.DECREASE_TRANSACTION_AMOUNT_LORE, Placeholders.AMOUNT),
                        arg1(DisplayKeys.CURRENT_TRANSACTION_AMOUNT, Placeholders.AMOUNT),
                        ClickResult.UPDATE_ICONS
                ),
                new UnitChangeButton(
                        47,
                        Amount.SHARED_DATA_KEY,
                        messageKey(DisplayKeys.CHANGE_UNIT),
                        messageKey(DisplayKeys.RESET_TRANSACTION_AMOUNT),
                        ClickResult.UPDATE_ICONS
                ),
                new IncreaseAmountButton(
                        48,
                        Amount.SHARED_DATA_KEY,
                        messageKey(DisplayKeys.INCREASE_TRANSACTION_AMOUNT_DISPLAY_NAME),
                        arg1(DisplayKeys.SET_TRANSACTION_AMOUNT_LORE, Placeholders.AMOUNT),
                        arg1(DisplayKeys.INCREASE_TRANSACTION_AMOUNT_LORE, Placeholders.AMOUNT),
                        arg1(DisplayKeys.CURRENT_TRANSACTION_AMOUNT, Placeholders.AMOUNT),
                        ClickResult.UPDATE_ICONS
                ),
                new BackOrCloseButton(49),
                new ModeButton(50)
        );
        TITLE = arg1(DisplayKeys.CATEGORY_MENU_TITLE, Placeholder.component("category", Function.identity()));
    }

    private final Category category;

    public CategoryMenu(@NotNull Category category) {
        super(6, category.getItems());
        this.category = category;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(this.category.getDisplayName(session.getViewer())).create(session.getMessageSource());
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItem instance, int slot) {
        return new BoxItemButton(instance, slot);
    }

    @Override
    protected void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons) {
        buttons.addAll(SHARED_BUTTONS);

        var mode = session.getBoxItemClickMode();
        if (mode.hasAdditionalButton()) {
            buttons.add(mode.createAdditionalButton(session, 52));
        }
    }

    public @NotNull Category getCategory() {
        return category;
    }
}
