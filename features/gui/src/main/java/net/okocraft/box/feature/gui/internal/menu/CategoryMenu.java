package net.okocraft.box.feature.gui.internal.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.buttons.amount.DecreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.IncreaseCustomNumberButton;
import net.okocraft.box.feature.gui.api.buttons.amount.UnitChangeButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.button.BoxItemButton;
import net.okocraft.box.feature.gui.internal.button.ModeButton;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryMenu extends AbstractPaginatedMenu<BoxItem> {

    private static final List<Button> SHARED_BUTTONS;

    static {
        SHARED_BUTTONS = List.of(
                new DecreaseAmountButton(
                        46,
                        Amount.SHARED_DATA_KEY,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT,
                        ClickResult.UPDATE_ICONS
                ),
                new UnitChangeButton(
                        47,
                        Amount.SHARED_DATA_KEY,
                        Displays.CHANGE_UNIT_BUTTON_DISPLAY_NAME,
                        Displays.CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_AMOUNT,
                        ClickResult.UPDATE_ICONS
                ),
                new IncreaseCustomNumberButton(
                        48,
                        Amount.SHARED_DATA_KEY,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_SET_TO_UNIT,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT,
                        ClickResult.UPDATE_ICONS
                ),
                new BackOrCloseButton(49),
                new ModeButton(50)
        );
    }

    private final Category category;

    public CategoryMenu(@NotNull Category category) {
        super(6, category.getItems());
        this.category = category;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return Displays.CATEGORY_MENU_TITLE.apply(category);
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
