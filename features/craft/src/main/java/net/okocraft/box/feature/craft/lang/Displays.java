package net.okocraft.box.feature.craft.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxItem;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.okocraft.box.feature.gui.internal.lang.Styles.NO_STYLE;

public final class Displays {

    public static final SingleArgument<BoxItem> CRAFT_MENU_TITLE =
            result ->
                    translatable()
                            .key("box.gui.menus.craft-menu.title")
                            .args(translatable(result.getOriginal()).style(NO_STYLE.color(BLACK)))
                            .color(BLACK)
                            .build();

    public static final SingleArgument<Integer> CRAFT_BUTTON_DISPLAY_NAME =
            times ->
                    translatable()
                            .key("box.gui.buttons.craft.display-name")
                            .args(text(times, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CRAFT_BUTTON_INGREDIENTS =
            translatable("box.gui.buttons.craft.ingredients", NO_STYLE.color(GRAY));

    public static final Component RECIPE_BUTTON_CLICK_TO_SHOW_DETAILS =
            translatable("box.gui.buttons.craft.click-to-show-details", NO_STYLE.color(GRAY));

    public static final SingleArgument<Integer> CRAFT_BUTTON_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.gui.buttons.craft.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component DISTRIBUTION_BUTTON_DISPLAY_NAME =
            translatable("box.gui.buttons.craft.distribution.display-name", NO_STYLE.color(GOLD));

    public static final Component DISTRIBUTION_BUTTON_INVENTORY =
            translatable("box.gui.buttons.craft.distribution.inventory", AQUA);

    public static final Component DISTRIBUTION_BUTTON_BOX =
            translatable("box.gui.buttons.craft.distribution.box", AQUA);

    public static final SingleArgument<Boolean> DISTRIBUTION_CURRENT =
            current ->
                    translatable()
                            .key("box.gui.buttons.craft.distribution.current")
                            .args(current ? DISTRIBUTION_BUTTON_INVENTORY : DISTRIBUTION_BUTTON_BOX)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Boolean> DISTRIBUTION_CLICK_TO_CHANGE =
            current ->
                    translatable()
                            .key("box.gui.buttons.craft.distribution.click-to-change")
                            .args(current ? DISTRIBUTION_BUTTON_BOX : DISTRIBUTION_BUTTON_INVENTORY)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CHANGE_UNIT_BUTTON_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.craft.change-unit.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final Component CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_TIMES =
            translatable("box.gui.buttons.craft.change-unit.shift-click-to-reset-times", NO_STYLE.color(GRAY));

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_CURRENT =
            currentAmount ->
                    translatable()
                            .key("box.gui.buttons.craft.change-times.current")
                            .args(text(currentAmount, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CHANGE_CRAFT_TIMES_BUTTON_INCREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.craft.change-times.increase.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_INCREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.craft.change-times.increase.lore")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final Component CHANGE_CRAFT_TIMES_BUTTON_DECREASE_DISPLAY_NAME =
            translatable()
                    .key("box.gui.buttons.craft.change-times.decrease.display-name")
                    .style(NO_STYLE)
                    .color(GOLD)
                    .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_DECREASE_LORE =
            unit ->
                    translatable()
                            .key("box.gui.buttons.craft.change-times.decrease.lore")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Integer> CHANGE_CRAFT_TIMES_BUTTON_SET_TO_UNIT =
            unit ->
                    translatable()
                            .key("box.gui.buttons.craft.change-times.set-to-unit")
                            .args(text(unit, AQUA))
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    public static final SingleArgument<BoxItem> RECIPE_SELECTOR_TITLE =
            result ->
                    translatable()
                            .key("box.gui.menus.recipe-selector.title")
                            .args(translatable(result.getOriginal()).style(NO_STYLE.color(BLACK)))
                            .style(NO_STYLE)
                            .color(BLACK)
                            .build();

    public static final Component CRAFT_MODE = translatable("box.gui.modes.craft-mode.display-name");

    public static final Component CLICK_TO_SHOW_RECIPES =
            translatable("box.gui.modes.craft-mode.click-to-show-recipes", NO_STYLE.color(GRAY));

    public static final Component RECIPE_NOT_FOUND =
            translatable("box.gui.modes.craft-mode.recipe-not-found", NO_STYLE.color(RED));

    public static final SingleArgument<BoxItem> RECIPE_NOT_FOUND_COMMAND =
            item ->
                    translatable()
                            .key("box.command.box.craft.recipe-not-found")
                            .args(item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()))
                            .style(NO_STYLE)
                            .color(RED)
                            .build();

    public static final Component BULK_INGREDIENT_CHANGE_MODE =
            translatable("box.gui.buttons.craft.ingredient-change-mode.bulk", NO_STYLE.color(GOLD));

    public static final Component EACH_INGREDIENT_CHANGE_MODE =
            translatable("box.gui.buttons.craft.ingredient-change-mode.each", NO_STYLE.color(GOLD));


    private Displays() {
        throw new UnsupportedOperationException();
    }
}
