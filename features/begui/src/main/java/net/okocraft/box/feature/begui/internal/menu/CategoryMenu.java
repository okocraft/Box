package net.okocraft.box.feature.begui.internal.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.BackButton;
import net.okocraft.box.feature.gui.api.buttons.customnumber.ChangeCustomNumberUnitButton;
import net.okocraft.box.feature.gui.api.buttons.customnumber.DecreaseCustomNumberButton;
import net.okocraft.box.feature.gui.api.buttons.customnumber.IncreaseCustomNumberButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.begui.internal.button.BoxItemButton;
import net.okocraft.box.feature.begui.internal.button.ModeButton;
import net.okocraft.box.feature.begui.internal.lang.Displays;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CategoryMenu extends AbstractPaginatedMenu<BoxItem> {

    private final Category category;
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);

    private final ModeButton modeButton = new ModeButton(50, updateFlag);
    private final BackButton backButton = new BackButton(new CategorySelectorMenu(), getRows() * 9 - 5);

    public CategoryMenu(@NotNull Category category) {
        super(category.getItems());
        this.category = category;
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public boolean shouldUpdate() {
        return updateFlag.getAndSet(false) || super.shouldUpdate();
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.CATEGORY_MENU_TITLE.apply(category);
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItem instance, int slot) {
        return new BoxItemButton(instance, slot, this);
    }

    @Override
    protected void addAdditionalButtons(@NotNull Player viewer, @NotNull List<Button> buttons) {
        buttons.add(backButton);

        var transactionAmountHolder = PlayerSession.get(viewer).getCustomNumberHolder("transaction-amount");

        buttons.add(
                new DecreaseCustomNumberButton(
                        transactionAmountHolder,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT,
                        46,
                        this
                )
        );

        buttons.add(
                new ChangeCustomNumberUnitButton(
                        transactionAmountHolder,
                        Displays.CHANGE_UNIT_BUTTON_DISPLAY_NAME,
                        Displays.CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_AMOUNT,
                        47,
                        this
                )
        );

        buttons.add(
                new IncreaseCustomNumberButton(
                        transactionAmountHolder,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_SET_TO_UNIT,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE,
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT,
                        48,
                        this
                )
        );

        var mode = PlayerSession.get(viewer).getBoxItemClickMode();

        modeButton.setIconMaterial(mode.getIconMaterial());
        buttons.add(modeButton);

        if (mode.hasAdditionalButton()) {
            var additionalButton = mode.createAdditionalButton(viewer, this);
            additionalButton.setSlot(52);
            buttons.add(additionalButton);
        }
    }
}
