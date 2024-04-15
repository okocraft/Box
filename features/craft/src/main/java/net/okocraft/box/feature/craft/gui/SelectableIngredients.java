package net.okocraft.box.feature.craft.gui;

import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A class for handling multiple {@link BoxIngredientItem}s that can be selected
 */
public class SelectableIngredients {

    private final IngredientHolder ingredientHolder;
    private final List<BoxIngredientItem> patterns;

    private int selected;

    public SelectableIngredients(@NotNull IngredientHolder ingredientHolder, @NotNull List<BoxIngredientItem> patterns) {
        this.ingredientHolder = ingredientHolder;
        this.patterns = patterns;
    }

    /**
     * Gets the currently selected {@link BoxIngredientItem}.
     *
     * @return the currently selected {@link BoxIngredientItem}
     */
    public @NotNull BoxIngredientItem getSelected() {
        return patterns.get(selected);
    }

    /**
     * Selects the next {@link BoxIngredientItem}.
     *
     * @return the position of the selected {@link BoxIngredientItem}
     */
    public int next() {
        selected++;

        if (patterns.size() <= selected) {
            selected = 0;
        }

        return selected;
    }

    /**
     * Selects the {@link BoxIngredientItem} at the specified position.
     *
     * @param num the position
     */
    public void select(int num) {
        selected = num;

        if (patterns.size() <= selected) {
            selected = 0;
        }
    }

    /**
     * Returns the {@link IngredientHolder#patterns()}.
     *
     * @return the {@link IngredientHolder#patterns()}
     */
    public @NotNull @Unmodifiable List<BoxIngredientItem> get() {
        return patterns;
    }

    /**
     * Returns the number of {@link BoxIngredientItem}s.
     *
     * @return the number of {@link BoxIngredientItem}s
     */
    public int size() {
        return patterns.size();
    }

    /**
     * Checks if this and the specified {@link SelectableIngredients} are same.
     *
     * @param other {@link SelectableIngredients} to check
     * @return {@code true} if two {@link SelectableIngredients} are same, otherwise {@code false}
     */
    public boolean isSameIngredient(@NotNull SelectableIngredients other) {
        return this.ingredientHolder.equals(other.ingredientHolder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectableIngredients that)) return false;
        return this.isSameIngredient(that);
    }

    @Override
    public int hashCode() {
        return this.ingredientHolder.hashCode();
    }
}
