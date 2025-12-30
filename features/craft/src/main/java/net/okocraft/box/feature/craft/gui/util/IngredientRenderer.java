package net.okocraft.box.feature.craft.gui.util;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public final class IngredientRenderer {

    private static final MessageKey INGREDIENTS_HEADER = MessageKey.key(DisplayKeys.INGREDIENTS_HEADER);

    public static void render(@NotNull ItemEditor editor, @NotNull PlayerSession session,
                              @NotNull SelectedRecipe recipe, int times) {
        editor.loreLine(INGREDIENTS_HEADER);

        Map<BoxItem, Integer> ingredientMap = new HashMap<>();

        for (BoxIngredientItem ingredient : recipe.ingredients()) {
            ingredientMap.put(
                ingredient.item(),
                ingredientMap.getOrDefault(ingredient.item(), 0) + ingredient.amount()
            );
        }

        for (Map.Entry<BoxItem, Integer> ingredient : ingredientMap.entrySet()) {
            BoxItem item = ingredient.getKey();
            int need = ingredient.getValue() * times;

            int current = session.getSourceStockHolder().getAmount(item);
            Style style = need <= current ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_RED;

            editor.loreLine(
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

    public static void render(@NotNull ItemEditor editor, @NotNull PlayerSession session,
                              @NotNull BoxItemRecipe recipe, int times, boolean simple) {
        if (simple) {
            List<BoxIngredientItem> ingredients =
                recipe.ingredients()
                    .stream()
                    .map(holder -> holder.patterns().getFirst())
                    .toList();

            render(editor, session, new SelectedRecipe(ingredients, recipe.result(), recipe.amount()), times);
            return;
        }

        Map<IngredientHolder, Integer> ingredientMap = new HashMap<>();

        for (IngredientHolder ingredient : recipe.ingredients()) {
            ingredientMap.put(ingredient, ingredientMap.getOrDefault(ingredient, 0) + 1);
        }

        editor.loreLine(INGREDIENTS_HEADER);

        int ingredientCounter = 0;
        int ingredientLast = ingredientMap.size();

        Style loreStyle = Styles.NO_DECORATION_GRAY;

        for (Map.Entry<IngredientHolder, Integer> ingredient : ingredientMap.entrySet()) {
            ingredientCounter++;
            IngredientHolder holder = ingredient.getKey();
            int need = ingredient.getValue() * times;

            List<TranslatableComponent> itemNames =
                holder.patterns().stream()
                    .map(BoxIngredientItem::item)
                    .map(BoxItem::getOriginal)
                    .map(Component::translatable)
                    .map(component -> component.style(loreStyle))
                    .toList();

            int itemNameCounter = 0;
            int itemNameLast = itemNames.size();

            for (TranslatableComponent itemName : itemNames) {
                TextComponent line = space().append(itemName);
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

                editor.loreLine(line);
            }

            if (ingredientCounter != ingredientLast) {
                editor.loreEmptyLine();
            }
        }
    }
}
