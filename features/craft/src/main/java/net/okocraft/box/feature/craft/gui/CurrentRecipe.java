package net.okocraft.box.feature.craft.gui;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CurrentRecipe {

    public static final TypedKey<CurrentRecipe> DATA_KEY = TypedKey.of(CurrentRecipe.class, "current_recipe");
    public static final TypedKey<Boolean> CHANGE_PER_INGREDIENT = TypedKey.of(Boolean.class, "change_per_ingredient");

    private final BoxItemRecipe source;
    private final Map<Integer, IngredientHolder.SelectableIngredients> ingredientsMap;

    private SelectedRecipe selectedRecipe;

    public CurrentRecipe(@NotNull BoxItemRecipe source) {
        this.source = source;
        this.ingredientsMap =
                source.ingredients().stream()
                        .filter(Predicate.not(ingredients -> ingredients.getPatterns().isEmpty()))
                        .collect(Collectors.toUnmodifiableMap(
                                IngredientHolder::getSlot,
                                IngredientHolder::toSelectableIngredients
                        ));

        updateSelectedRecipe();
    }

    public @NotNull BoxItem getResult() {
        return source.result();
    }

    public @NotNull ItemStack getResultPreview() {
        return source.result().getOriginal().asQuantity(source.amount());
    }

    public void nextRecipe(int pos, boolean sameIngredient) {
        var recipe = ingredientsMap.get(pos);

        if (recipe == null) {
            return;
        }

        var selected = recipe.next();

        if (sameIngredient) {
            for (var other : ingredientsMap.values()) {
                if (recipe != other && recipe.isSameIngredient(other)) {
                    other.select(selected);
                }
            }
        }

        updateSelectedRecipe();
    }

    public @Nullable IngredientHolder.SelectableIngredients getIngredients(int pos) {
        return ingredientsMap.get(pos);
    }

    public void updateSelectedRecipe() {
        this.selectedRecipe =
                new SelectedRecipe(
                        ingredientsMap.values().stream().map(IngredientHolder.SelectableIngredients::getSelected).toList(),
                        source.result(),
                        source.amount()
                );
    }

    public @NotNull SelectedRecipe getSelectedRecipe() {
        return selectedRecipe;
    }
}
