package net.okocraft.box.feature.craft.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxItem;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_RED;

public final class Displays {

    public static final SingleArgument<BoxItem> CRAFT_MENU_TITLE =
            result ->
                    translatable()
                            .key("box.craft.gui.menus.craft")
                            .args(translatable(result.getOriginal(), BLACK))
                            .color(BLACK)
                            .build();

    public static final SingleArgument<BoxItem> RECIPE_SELECTOR_TITLE =
            result ->
                    translatable()
                            .key("box.craft.gui.menus.recipe-selector")
                            .args(translatable(result.getOriginal(), BLACK))
                            .color(BLACK)
                            .build();

    public static final SingleArgument<Integer> CRAFT_BUTTON_DISPLAY_NAME =
            times ->
                    translatable()
                            .key("box.craft.gui.buttons.display-name")
                            .args(text(times, NO_DECORATION_AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CRAFT_BUTTON_INGREDIENTS =
            translatable("box.craft.gui.buttons.ingredients", NO_DECORATION_GRAY);

    public static final Component RECIPE_BUTTON_CLICK_TO_SHOW_DETAILS =
            translatable("box.craft.gui.buttons.click-to-show-details", NO_DECORATION_GRAY);

    public static final SingleArgument<Integer> CRAFT_BUTTON_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.craft.gui.buttons.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component DISTRIBUTION_BUTTON_DISPLAY_NAME =
            translatable("box.craft.gui.buttons.distribution.display-name", NO_DECORATION_GOLD);

    public static final Component DISTRIBUTION_BUTTON_INVENTORY =
            translatable("box.craft.gui.buttons.distribution.inventory", AQUA);

    public static final Component DISTRIBUTION_BUTTON_BOX =
            translatable("box.craft.gui.buttons.distribution.box", AQUA);

    public static final SingleArgument<Boolean> DISTRIBUTION_CURRENT =
            current ->
                    translatable()
                            .key("box.craft.gui.buttons.distribution.current")
                            .args(current ? DISTRIBUTION_BUTTON_INVENTORY : DISTRIBUTION_BUTTON_BOX)
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Boolean> DISTRIBUTION_CLICK_TO_CHANGE =
            current ->
                    translatable()
                            .key("box.craft.gui.buttons.distribution.click-to-change")
                            .args(current ? DISTRIBUTION_BUTTON_BOX : DISTRIBUTION_BUTTON_INVENTORY)
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_UNIT_BUTTON_DISPLAY_NAME =
            translatable()
                    .key("box.craft.gui.buttons.change-unit.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_TIMES =
            translatable("box.craft.gui.buttons.change-unit.shift-click-to-reset-times", NO_DECORATION_GRAY);

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_CURRENT =
            currentAmount ->
                    translatable()
                            .key("box.craft.gui.buttons.change-times.current")
                            .args(text(currentAmount, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_CRAFT_TIMES_BUTTON_INCREASE_DISPLAY_NAME =
            translatable()
                    .key("box.craft.gui.buttons.change-times.increase.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_INCREASE_LORE =
            unit ->
                    translatable()
                            .key("box.craft.gui.buttons.change-times.increase.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CHANGE_CRAFT_TIMES_BUTTON_DECREASE_DISPLAY_NAME =
            translatable()
                    .key("box.craft.gui.buttons.change-times.decrease.display-name")
                    .style(NO_DECORATION_GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_DECREASE_LORE =
            unit ->
                    translatable()
                            .key("box.craft.gui.buttons.change-times.decrease.lore")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_SET_TO_UNIT =
            unit ->
                    translatable()
                            .key("box.craft.gui.buttons.change-times.set-to-unit")
                            .args(text(unit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component CRAFT_MODE = translatable("box.craft.gui.mode.display-name");

    public static final Component CLICK_TO_SHOW_RECIPES =
            translatable("box.craft.gui.mode.click-to-show-recipes", NO_DECORATION_GRAY);

    public static final Component RECIPE_NOT_FOUND =
            translatable("box.craft.gui.mode.recipe-not-found", NO_DECORATION_RED);

    public static final Component BULK_INGREDIENT_CHANGE_MODE =
            translatable("box.craft.gui.buttons.ingredient-change-mode.bulk", NO_DECORATION_GOLD);

    public static final Component EACH_INGREDIENT_CHANGE_MODE =
            translatable("box.craft.gui.buttons.ingredient-change-mode.each", NO_DECORATION_GOLD);

    public static final SingleArgument<BoxItem> COMMAND_RECIPE_NOT_FOUND =
            item ->
                    translatable()
                            .key("box.craft.command.recipe-not-found")
                            .args(item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()))
                            .color(RED)
                            .build();

    public static final Component COMMAND_HELP =
            translatable("box.craft.command.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.craft.command.help.description", GRAY));

    private Displays() {
        throw new UnsupportedOperationException();
    }
}
