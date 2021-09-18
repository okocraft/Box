package net.okocraft.box.feature.gui.internal.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public final class Displays {

    public static final Component BACK_BUTTON =
            translatable()
                    .key("box.gui.buttons.back")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CLOSE_BUTTON =
            translatable()
                    .key("box.gui.buttons.close")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component MODE_BUTTON =
            translatable()
                    .key("box.gui.buttons.mode-change")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component PAGE_SWITCH_BUTTON_PREVIOUS =
            translatable()
                    .key("box.gui.buttons.page-switch.previous")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component PAGE_SWITCH_BUTTON_NEXT =
            translatable()
                    .key("box.gui.buttons.page-switch.next")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-unit.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_AMOUNT =
            translatable("box.gui.buttons.change-unit.shift-click-to-reset-amount", NO_DECORATION_GRAY);

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT =
            currentAmount ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.current")
                            .args(text(currentAmount, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-transaction-amount.increase.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.increase.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-transaction-amount.decrease.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.decrease.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_SET_TO_UNIT =
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.set-to-unit")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CATEGORY_SELECTOR_MENU =
            translatable("box.gui.menus.category-selector", BLACK);

    public static final DoubleArgument<Category, BoxItemClickMode> CATEGORY_MENU_TITLE =
            (category, mode) ->
                    translatable()
                            .key("box.gui.menus.category-menu")
                            .args(
                                    category.getDisplayName().color(BLACK),
                                    mode.getDisplayName().color(BLACK)
                            )
                            .color(BLACK)
                            .build();

    public static final Component STORAGE_MODE_DISPLAY_NAME =
            translatable("box.gui.modes.storage-mode.display-name");

    public static final SingleArgument<Integer> STORAGE_MODE_LEFT_CLICK_TO_DEPOSIT =
            transactionUnit ->
                    translatable()
                            .key("box.gui.modes.storage-mode.left-click-to-deposit")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_RIGHT_CLICK_TO_WITHDRAW =
            transactionUnit ->
                    translatable()
                            .key("box.gui.modes.storage-mode.right-click-to-withdraw")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.gui.modes.storage-mode.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();
}
