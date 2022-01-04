package net.okocraft.box.feature.craft.util;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;


public final class IngredientRenderer {

    public static void render(@NotNull List<Component> target, @NotNull SelectedRecipe recipe,
                              @NotNull Player viewer, int times) {
        var stockHolder = PlayerSession.get(viewer).getStockHolder();

        target.add(TranslationUtil.render(Displays.CRAFT_BUTTON_INGREDIENTS.append(text(":")), viewer));

        var ingredientMap = new HashMap<BoxItem, Integer>();

        for (var ingredient : recipe.ingredients()) {
            ingredientMap.put(
                    ingredient.item(),
                    ingredientMap.getOrDefault(ingredient.item(), 0) + ingredient.amount()
            );
        }

        for (var ingredient : ingredientMap.entrySet()) {
            var item = ingredient.getKey();
            int need = ingredient.getValue() * times;

            int current = stockHolder.getAmount(item);
            var style = need <= current ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_RED;

            target.add(
                    space().toBuilder()
                            .append(translatable(item.getOriginal(), style))
                            .append(text(": ", Styles.NO_DECORATION_GRAY))
                            .append(text(need, style))
                            .append(text("/", Styles.NO_DECORATION_GRAY))
                            .append(text(current, style))
                            .build()
            );
        }
    }

    public static void render(@NotNull List<Component> target, @NotNull BoxItemRecipe recipe,
                              @NotNull Player viewer, int times, boolean simple) {
        if (simple) {
            var ingredients =
                    recipe.ingredients()
                            .stream()
                            .map(holder -> holder.getPatterns().get(0))
                            .toList();

            render(target, new SelectedRecipe(ingredients, recipe.result(), recipe.amount()), viewer, times);
            return;
        }

        var ingredientMap = new HashMap<IngredientHolder, Integer>();

        for (var ingredient : recipe.ingredients()) {
            ingredientMap.put(ingredient, ingredientMap.getOrDefault(ingredient, 0) + 1);
        }

        target.add(TranslationUtil.render(Displays.CRAFT_BUTTON_INGREDIENTS.append(text(":")), viewer));

        int ingredientCounter = 0;
        int ingredientLast = ingredientMap.size();

        var loreStyle = Styles.NO_DECORATION_GRAY;

        for (var ingredient : ingredientMap.entrySet()) {
            ingredientCounter++;
            var holder = ingredient.getKey();
            int need = ingredient.getValue() * times;

            var itemNames =
                    holder.getPatterns().stream()
                            .map(BoxIngredientItem::item)
                            .map(BoxItem::getOriginal)
                            .map(Component::translatable)
                            .map(component -> component.style(loreStyle))
                            .toList();

            int itemNameCounter = 0;
            int itemNameLast = itemNames.size();

            for (var itemName : itemNames) {
                var line = space().append(itemName);
                itemNameCounter++;

                if (itemNameCounter != itemNameLast) {
                    line = line.append(text(", ", loreStyle));
                } else {
                    line = line.append(
                            text().append(space())
                                    .append(text("x", loreStyle))
                                    .append(text(need, Styles.NO_DECORATION_AQUA))
                    );
                }

                target.add(line);
            }

            if (ingredientCounter != ingredientLast) {
                target.add(empty());
            }
        }
    }
}
