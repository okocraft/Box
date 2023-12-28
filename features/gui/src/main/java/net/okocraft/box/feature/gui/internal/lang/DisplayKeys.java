package net.okocraft.box.feature.gui.internal.lang;

import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.NotNull;

public final class DisplayKeys {

    public static final String BACK = "box.gui.buttons.back";
    public static final String CLOSE = "box.gui.buttons.close";
    public static final String MODE_CHANGE = "box.gui.buttons.mode-change";
    public static final String PREVIOUS_PAGE = "box.gui.buttons.previous-page";
    public static final String NEXT_PAGE = "box.gui.buttons.next-page";

    public static final String CATEGORY_SELECTOR_MENU_TITLE = "box.gui.menus.category-selector.title";

    public static final String CATEGORY_MENU_TITLE = "box.gui.menus.category.title";
    public static final String CURRENT_TRANSACTION_AMOUNT = "box.gui.menus.category.buttons.transaction-amount.current";
    public static final String DECREASE_TRANSACTION_AMOUNT_DISPLAY_NAME = "box.gui.menus.category.buttons.transaction-amount.decrease.display-name";
    public static final String DECREASE_TRANSACTION_AMOUNT_LORE = "box.gui.menus.category.buttons.transaction-amount.decrease.lore";
    public static final String INCREASE_TRANSACTION_AMOUNT_DISPLAY_NAME = "box.gui.menus.category.buttons.transaction-amount.increase.display-name";
    public static final String INCREASE_TRANSACTION_AMOUNT_LORE = "box.gui.menus.category.buttons.transaction-amount.increase.lore";
    public static final String SET_TRANSACTION_AMOUNT_LORE = "box.gui.menus.category.buttons.transaction-amount.set.lore";
    public static final String CHANGE_UNIT = "box.gui.menus.category.buttons.transaction-amount.change-unit.display-name";
    public static final String RESET_TRANSACTION_AMOUNT = "box.gui.menus.category.buttons.transaction-amount.click-to-reset";

    public static void addDefaults(@NotNull DefaultMessageCollector collector) {
        collector.add(BACK, "<gold>Back");
        collector.add(CLOSE, "<gold>Close");
        collector.add(MODE_CHANGE, "<gold>Change mode");
        collector.add(PREVIOUS_PAGE, "<gold>Previous page");
        collector.add(NEXT_PAGE, "<gold>Next page");

        collector.add(CATEGORY_SELECTOR_MENU_TITLE, "<black>Category Selector");

        collector.add(CATEGORY_MENU_TITLE, "<black><category>");
        collector.add(CURRENT_TRANSACTION_AMOUNT, "<gray>Current transaction amount: <aqua><amount>");
        collector.add(DECREASE_TRANSACTION_AMOUNT_DISPLAY_NAME, "<gold>Decrease");
        collector.add(DECREASE_TRANSACTION_AMOUNT_LORE, "<gray>Click to decrease transaction amount by <aqua><amount>");
        collector.add(INCREASE_TRANSACTION_AMOUNT_DISPLAY_NAME, "<gold>Increase");
        collector.add(INCREASE_TRANSACTION_AMOUNT_LORE, "<gray>Click to increase transaction amount by <aqua><amount>");
        collector.add(SET_TRANSACTION_AMOUNT_LORE, "<gray>Click to set transaction amount to <aqua><amount>");
        collector.add(CHANGE_UNIT, "<gold>Change unit");
        collector.add(RESET_TRANSACTION_AMOUNT, "<gray>Shift + click to reset transaction amount");
    }
}
