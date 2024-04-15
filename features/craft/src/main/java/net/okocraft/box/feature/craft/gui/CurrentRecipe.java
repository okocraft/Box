package net.okocraft.box.feature.craft.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CurrentRecipe {

    public static final TypedKey<CurrentRecipe> DATA_KEY = TypedKey.of(CurrentRecipe.class, "current_recipe");

    private final BoxItemRecipe source;
    private final Int2ObjectMap<SelectableIngredients> ingredientsMap = new Int2ObjectOpenHashMap<>();

    private SelectedRecipe selectedRecipe;
    private Comparator<BoxIngredientItem> ingredientSorter;

    public CurrentRecipe(@NotNull BoxItemRecipe source) {
        this.source = source;

        this.resetIngredientMap();
        this.updateSelectedRecipe();
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

    public @Nullable SelectableIngredients getIngredients(int pos) {
        return ingredientsMap.get(pos);
    }

    public void updateSelectedRecipe() {
        this.selectedRecipe =
                new SelectedRecipe(
                        ingredientsMap.values().stream().map(SelectableIngredients::getSelected).toList(),
                        source.result(),
                        source.amount()
                );
    }

    public @NotNull SelectedRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void changeIngredientSorter(Comparator<BoxIngredientItem> ingredientSorter) {
        this.ingredientSorter = ingredientSorter;
        this.resetIngredientMap();
        this.updateSelectedRecipe();
    }

    private void resetIngredientMap() {
        for (var holder : this.source.ingredients()) {
            if (holder.patterns().isEmpty()) {
                continue;
            }

            List<BoxIngredientItem> patterns;

            if (this.ingredientSorter != null) {
                patterns = new ArrayList<>(holder.patterns());
                patterns.sort(this.ingredientSorter);
            } else {
                patterns = holder.patterns();
            }

            this.ingredientsMap.put(holder.slot(), new SelectableIngredients(holder, patterns));
        }
    }
}
