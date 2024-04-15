package net.okocraft.box.feature.craft.lang;

import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.NotNull;

public final class DisplayKeys {

    public static final String RECIPE_SELECTOR_TITLE = "box.craft.menu.recipe-selector.title";
    public static final String CLICK_TO_SHOW_DETAILS = "box.craft.menu.recipe-selector.buttons.recipe-select.click-to-show-details";

    public static final String CRAFT_MENU_TITLE = "box.craft.menu.craft-menu.title";

    public static final String CRAFT_BUTTON = "box.craft.menu.craft-menu.buttons.craft.display-name";
    public static final String CRAFT_BUTTON_CURRENT_STOCK = "box.craft.menu.craft-menu.buttons.craft.current-stock";

    public static final String INGREDIENT_BUTTON_CLICK_TO_SHOW_RECIPES = "box.craft.menu.craft-menu.buttons.ingredient.click-to-show-recipes";

    public static final String EACH_INGREDIENT_MODE = "box.craft.menu.craft-menu.buttons.change-mode.each-ingredient";
    public static final String ALL_INGREDIENT_MODE = "box.craft.menu.craft-menu.buttons.change-mode.all-ingredient";

    public static final String DESTINATION_BUTTON = "box.craft.menu.craft-menu.buttons.destination.display-name";
    public static final String INVENTORY = "box.craft.menu.craft-menu.buttons.destination.inventory";
    public static final String BOX = "box.craft.menu.craft-menu.buttons.destination.box";
    public static final String CHANGE_TO_INVENTORY = "box.craft.menu.craft-menu.buttons.destination.change-to.inventory";
    public static final String CHANGE_TO_BOX = "box.craft.menu.craft-menu.buttons.destination.change-to.box";

    public static final String CURRENT_CRAFT_TIMES = "box.craft.menu.craft-menu.buttons.change-times.current";
    public static final String DECREASE_CRAFT_TIMES_DISPLAY_NAME = "box.craft.menu.craft-menu.buttons.change-times.decrease.display-name";
    public static final String DECREASE_CRAFT_TIMES_LORE = "box.craft.menu.craft-menu.buttons.change-times.decrease.lore";
    public static final String INCREASE_CRAFT_TIMES_DISPLAY_NAME = "box.craft.menu.craft-menu.buttons.change-times.increase.display-name";
    public static final String INCREASE_CRAFT_TIMES_LORE = "box.craft.menu.craft-menu.buttons.change-times.increase.lore";
    public static final String SET_CRAFT_TIMES_LORE = "box.craft.menu.craft-menu.buttons.change-times.set.lore";

    public static final String CHANGE_UNIT = "box.craft.menu.craft-menu.buttons.change-unit.display-name";
    public static final String RESET_CRAFT_TIMES = "box.craft.menu.craft-menu.buttons.change-unit.click-to-reset-craft-times";

    public static final String INGREDIENTS_HEADER = "box.craft.menu.shared.ingredient-header";


    public static void addDefaults(@NotNull DefaultMessageCollector collector) {
        collector.add(RECIPE_SELECTOR_TITLE, "<black>Recipes of <item>");
        collector.add(CLICK_TO_SHOW_DETAILS, "<gray>Shift + click to show details");

        collector.add(CRAFT_MENU_TITLE, "<black>Craft of <item>");
        collector.add(CRAFT_BUTTON, "<aqua>Craft <times> times");
        collector.add(CRAFT_BUTTON_CURRENT_STOCK, "<gray>Current stock: <aqua><current>");
        collector.add(INGREDIENT_BUTTON_CLICK_TO_SHOW_RECIPES, "<gray>Shift + Click to show recipes");
        collector.add(EACH_INGREDIENT_MODE, "<gold>Each Ingredient Change Mode");
        collector.add(ALL_INGREDIENT_MODE, "<gold>All Ingredient Change Mode");
        collector.add(DESTINATION_BUTTON, "<gold>Destination of items");
        collector.add(INVENTORY, "<gray>Inventory");
        collector.add(BOX, "<gray>Box");
        collector.add(CHANGE_TO_INVENTORY, "<gray>Click to change to <aqua>inventory");
        collector.add(CHANGE_TO_BOX, "<gray>Click to change to <aqua>Box");
        collector.add(CURRENT_CRAFT_TIMES, "<gray>Current craft times: <aqua><times>");
        collector.add(DECREASE_CRAFT_TIMES_DISPLAY_NAME, "<gold>Decrease");
        collector.add(DECREASE_CRAFT_TIMES_LORE, "<gray>Click to decrease craft times by <aqua><times>");
        collector.add(INCREASE_CRAFT_TIMES_DISPLAY_NAME, "<gold>Increase");
        collector.add(INCREASE_CRAFT_TIMES_LORE, "<gray>Click to increase craft times by <aqua><times>");
        collector.add(SET_CRAFT_TIMES_LORE, "<gray>Click to set craft times to <aqua><times>");
        collector.add(CHANGE_UNIT, "<gold>Change unit");
        collector.add(RESET_CRAFT_TIMES, "<gray>Shift + click to reset craft times");

        collector.add(INGREDIENTS_HEADER, "<gray>Ingredients:");
    }
}
