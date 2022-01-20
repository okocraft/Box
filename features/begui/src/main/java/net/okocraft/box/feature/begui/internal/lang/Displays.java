package net.okocraft.box.feature.begui.internal.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.category.model.Category;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public final class Displays {

    public static final Component MODE_BUTTON =
            translatable()
                    .key("box.begui.buttons.mode-change")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_DISPLAY_NAME =
            translatable()
                    .key("box.begui.buttons.change-unit.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_AMOUNT =
            translatable("box.begui.buttons.change-unit.shift-click-to-reset-amount", NO_DECORATION_GRAY);

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT =
            currentAmount ->
                    translatable()
                            .key("box.begui.buttons.change-transaction-amount.current")
                            .args(text(currentAmount, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME =
            translatable()
                    .key("box.begui.buttons.change-transaction-amount.increase.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE =
            unit ->
                    translatable()
                            .key("box.begui.buttons.change-transaction-amount.increase.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME =
            translatable()
                    .key("box.begui.buttons.change-transaction-amount.decrease.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE =
            unit ->
                    translatable()
                            .key("box.begui.buttons.change-transaction-amount.decrease.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_SET_TO_UNIT =
            unit ->
                    translatable()
                            .key("box.begui.buttons.change-transaction-amount.set-to-unit")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CATEGORY_SELECTOR_MENU =
            translatable("box.begui.menus.category-selector", BLACK);

    public static final SingleArgument<Category> CATEGORY_MENU_TITLE =
            category ->
                    translatable()
                            .key("box.begui.menus.category-menu")
                            .args(category.getDisplayName().color(BLACK))
                            .color(BLACK)
                            .build();

    public static final Component STORAGE_MODE_WITHDRAW_DISPLAY_NAME =
            translatable("box.begui.modes.storage-mode.withdraw.display-name");

    public static final Component STORAGE_MODE_DEPOSIT_DISPLAY_NAME =
            translatable("box.begui.modes.storage-mode.deposit.display-name");

    public static final SingleArgument<Integer> STORAGE_MODE_WITHDRAW_CLICK_TO_WITHDRAW =
            transactionUnit ->
                    translatable()
                            .key("box.begui.modes.storage-mode.withdraw.click-to-withdraw")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_DEPOSIT_CLICK_TO_DEPOSIT =
            transactionUnit ->
                    translatable()
                            .key("box.begui.modes.storage-mode.deposit.click-to-deposit")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.begui.modes.storage-mode.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component STORAGE_MODE_DEPOSIT_ALL_BUTTON_DISPLAY_NAME =
            translatable("box.begui.modes.storage-mode.deposit-all.display-name", NO_DECORATION_GOLD);

    public static final Component STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_1 =
            translatable("box.begui.modes.storage-mode.deposit-all.lore-1", NO_DECORATION_GRAY);

    public static final Component STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_2 =
            translatable("box.begui.modes.storage-mode.deposit-all.lore-2", NO_DECORATION_GRAY);

    public static final SingleArgument<Throwable> ERROR_WHILE_CLICK_PROCESSING =
            throwable ->
                    Component.translatable()
                            .key("box.begui.error-occurred")
                            .args(Component.text(throwable.getMessage(), NamedTextColor.WHITE))
                            .color(NamedTextColor.RED)
                            .build();
}
