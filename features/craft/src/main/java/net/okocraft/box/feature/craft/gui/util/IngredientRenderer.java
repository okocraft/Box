package net.okocraft.box.feature.craft.gui.util;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public final class IngredientRenderer {

    private static final MiniMessageBase INGREDIENTS_HEADER = MiniMessageBase.messageKey(DisplayKeys.INGREDIENTS_HEADER);

    public static void render(@NotNull ItemEditor<? extends ItemMeta> editor, @NotNull PlayerSession session,
                              @NotNull SelectedRecipe recipe, int times) {
        editor.loreLine(INGREDIENTS_HEADER.create(session.getMessageSource()));

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

            int current = session.getSourceStockHolder().getAmount(item);
            var style = need <= current ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_RED;

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

    public static void render(@NotNull ItemEditor<? extends ItemMeta> editor, @NotNull PlayerSession session,
                              @NotNull BoxItemRecipe recipe, int times, boolean simple) {
        if (simple) {
            var ingredients =
                    recipe.ingredients()
                            .stream()
                            .map(holder -> holder.patterns().getFirst())
                            .toList();

            render(editor, session, new SelectedRecipe(ingredients, recipe.result(), recipe.amount()), times);
            return;
        }

        var ingredientMap = new HashMap<IngredientHolder, Integer>();

        for (var ingredient : recipe.ingredients()) {
            ingredientMap.put(ingredient, ingredientMap.getOrDefault(ingredient, 0) + 1);
        }

        editor.loreLine(INGREDIENTS_HEADER.create(session.getMessageSource()));

        int ingredientCounter = 0;
        int ingredientLast = ingredientMap.size();

        var loreStyle = Styles.NO_DECORATION_GRAY;

        for (var ingredient : ingredientMap.entrySet()) {
            ingredientCounter++;
            var holder = ingredient.getKey();
            int need = ingredient.getValue() * times;

            var itemNames =
                    holder.patterns().stream()
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

                editor.loreLine(line);
            }

            if (ingredientCounter != ingredientLast) {
                editor.loreEmptyLine();
            }
        }
    }
}
