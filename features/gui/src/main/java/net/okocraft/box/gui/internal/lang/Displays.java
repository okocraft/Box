package net.okocraft.box.gui.internal.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.gui.api.mode.BoxItemClickMode;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.okocraft.box.gui.internal.lang.Styles.NO_STYLE;

public final class Displays {

    public static final Component BACK_BUTTON =
            translatable()
                    .key("box.gui.buttons.back")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component CLOSE_BUTTON =
            translatable()
                    .key("box.gui.buttons.close")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component MODE_BUTTON =
            translatable()
                    .key("box.gui.buttons.mode-change")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component PAGE_SWITCH_BUTTON_PREVIOUS =
            translatable()
                    .key("box.gui.buttons.page-switch.previous")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component PAGE_SWITCH_BUTTON_NEXT =
            translatable()
                    .key("box.gui.buttons.page-switch.next")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-unit.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_AMOUNT =
            translatable("box.gui.buttons.change-unit.shift-click-to-reset-amount", NO_STYLE.color(GRAY));

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT =
            currentAmount ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.current")
                            .args(text(currentAmount, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-transaction-amount.increase.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.increase.lore")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.change-transaction-amount.decrease.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.decrease.lore")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Integer> CHANGE_TRANSACTION_AMOUNT_BUTTON_SET_TO_UNIT=
            unit ->
                    translatable()
                            .key("box.gui.buttons.change-transaction-amount.set-to-unit")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CATEGORY_SELECTOR_MENU =
            translatable()
                    .key("box.gui.menus.category-selector")
                    .style(NO_STYLE)
                    .color(BLACK)
                    .build();

    public static final DoubleArgument<Category, BoxItemClickMode> CATEGORY_MENU_TITLE =
            (category, mode) ->
                    translatable()
                            .key("box.gui.menus.category-menu.title")
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
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_RIGHT_CLICK_TO_WITHDRAW =
            transactionUnit ->
                    translatable()
                            .key("box.gui.modes.storage-mode.right-click-to-withdraw")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_MODE_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.gui.modes.storage-mode.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component AUTOSTORE_MODE_DISPLAY_NAME =
            translatable("box.gui.modes.autostore-mode.display-name");

    public static final Component AUTOSTORE_MODE_ENABLED =
            translatable("box.gui.modes.autostore-mode.enabled").color(GREEN);

    public static final Component AUTOSTORE_MODE_DISABLED =
            translatable("box.gui.modes.autostore-mode.disabled").color(RED);

    public static final SingleArgument<Boolean> AUTOSTORE_MODE_LORE =
            enabled ->
                    translatable()
                            .key("box.gui.modes.autostore-mode.lore")
                            .args(enabled ? AUTOSTORE_MODE_ENABLED : AUTOSTORE_MODE_DISABLED)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component AUTOSTORE_MODE_SETTING_MENU_TITLE =
            translatable("box.gui.modes.autostore-mode.setting-menu.title", BLACK);

    public static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.display-name", NO_STYLE.color(GOLD));

    public static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.all", NO_STYLE.color(GRAY));

    public static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.per-item", NO_STYLE.color(GRAY));

    public static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE =
            translatable("box.gui.modes.autostore-mode.setting-menu.bulk-editing.title", NO_STYLE.color(GOLD));

    public static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_LEFT_CLICK =
            translatable()
                    .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.left-click")
                    .args(AUTOSTORE_MODE_ENABLED)
                    .style(NO_STYLE)
                    .color(GRAY)
                    .build();

    public static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RIGHT_CLICK =
            translatable()
                    .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.right-click")
                    .args(AUTOSTORE_MODE_DISABLED)
                    .style(NO_STYLE)
                    .color(GRAY)
                    .build();

    public static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RECENT =
            enabled ->
                    translatable()
                            .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.recent")
                            .args(enabled ? AUTOSTORE_MODE_ENABLED : AUTOSTORE_MODE_DISABLED)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();
}
