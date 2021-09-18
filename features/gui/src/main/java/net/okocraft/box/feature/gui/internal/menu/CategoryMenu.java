package net.okocraft.box.feature.gui.internal.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.BackButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.internal.button.BoxItemButton;
import net.okocraft.box.feature.gui.internal.button.ChangeTransactionAmountButton;
import net.okocraft.box.feature.gui.internal.button.ChangeUnitButton;
import net.okocraft.box.feature.gui.internal.button.ModeButton;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryMenu extends AbstractPaginatedMenu<BoxItem> {

    private final Category category;
    private final ModeButton modeButton = new ModeButton(50);
    private final BackButton backButton = new BackButton(new CategorySelectorMenu(), getRows() * 9 - 5);

    private final ChangeTransactionAmountButton decreaseTransactionAmountButton =
            new ChangeTransactionAmountButton(46, false, this);

    private final ChangeUnitButton changeUnitButton = new ChangeUnitButton(47, this);

    private final ChangeTransactionAmountButton increaseTransactionAmountButton =
            new ChangeTransactionAmountButton(48, true, this);

    public CategoryMenu(@NotNull Category category) {
        super(category.getItems());
        this.category = category;
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.CATEGORY_MENU_TITLE.apply(category, modeButton.getCurrentMode());
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItem instance, int slot) {
        return new BoxItemButton(instance, slot, modeButton::getCurrentMode, this);
    }

    @Override
    protected void addAdditionalButtons(@NotNull Player viewer, @NotNull List<Button> buttons) {
        buttons.add(modeButton);
        buttons.add(backButton);

        buttons.add(decreaseTransactionAmountButton);
        buttons.add(changeUnitButton);
        buttons.add(increaseTransactionAmountButton);

        var mode = modeButton.getCurrentMode();

        if (mode.hasAdditionalButton()) {
            var additionalButton = mode.createAdditionalButton(viewer, this);
            additionalButton.setSlot(52);
            buttons.add(additionalButton);
        }
    }
}
