package net.okocraft.box.feature.craft.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class CurrentRecipe {

    public static final TypedKey<CurrentRecipe> DATA_KEY = TypedKey.of(CurrentRecipe.class, "current_recipe");

    private final BoxItemRecipe source;
    private final Int2ObjectMap<SelectableIngredients> ingredientsMap = new Int2ObjectOpenHashMap<>();

    private SelectedRecipe selectedRecipe;

    public CurrentRecipe(@NotNull BoxItemRecipe source) {
        this.source = source;

        this.sortIngredients(null);
        this.updateSelectedRecipe();
    }

    public @NotNull BoxItem getResult() {
        return this.source.result();
    }

    public @NotNull ItemStack getResultPreview() {
        return this.source.result().getOriginal().asQuantity(this.source.amount());
    }

    public void nextRecipe(int pos, boolean sameIngredient) {
        SelectableIngredients recipe = this.ingredientsMap.get(pos);

        if (recipe == null) {
            return;
        }

        int selected = recipe.next();

        if (sameIngredient) {
            for (SelectableIngredients other : this.ingredientsMap.values()) {
                if (recipe != other && recipe.isSameIngredient(other)) {
                    other.select(selected);
                }
            }
        }

        this.updateSelectedRecipe();
    }

    public @Nullable SelectableIngredients getIngredients(int pos) {
        return this.ingredientsMap.get(pos);
    }

    public void updateSelectedRecipe() {
        this.selectedRecipe =
            new SelectedRecipe(
                this.ingredientsMap.values().stream().map(SelectableIngredients::getSelected).toList(),
                this.source.result(),
                this.source.amount()
            );
    }

    public @NotNull SelectedRecipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    public void selectFirstIngredients() {
        this.ingredientsMap.values().forEach(ingredients -> ingredients.select(0));
        this.updateSelectedRecipe();
    }

    public void sortIngredients(@Nullable Comparator<BoxIngredientItem> sorter) {
        for (IngredientHolder holder : this.source.ingredients()) {
            if (holder.patterns().isEmpty()) {
                continue;
            }

            SelectableIngredients existing = this.ingredientsMap.get(holder.slot());

            if (existing != null) {
                existing.sortPatterns(sorter);
            } else {
                SelectableIngredients ingredients = new SelectableIngredients(holder);
                ingredients.sortPatterns(sorter);
                this.ingredientsMap.put(holder.slot(), ingredients);
            }
        }
    }
}
