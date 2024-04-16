package net.okocraft.box.feature.craft.gui;

import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A class for handling multiple {@link BoxIngredientItem}s that can be selected
 */
public class SelectableIngredients {

    private final IngredientHolder ingredientHolder;

    private List<BoxIngredientItem> sortedPatterns;
    private int selected;

    public SelectableIngredients(@NotNull IngredientHolder ingredientHolder) {
        this.ingredientHolder = ingredientHolder;
        this.sortedPatterns = ingredientHolder.patterns();
    }

    /**
     * Gets the currently selected {@link BoxIngredientItem}.
     *
     * @return the currently selected {@link BoxIngredientItem}
     */
    public @NotNull BoxIngredientItem getSelected() {
        return this.sortedPatterns.get(this.selected);
    }

    /**
     * Selects the next {@link BoxIngredientItem}.
     *
     * @return the position of the selected {@link BoxIngredientItem}
     */
    public int next() {
        this.selected++;

        if (this.sortedPatterns.size() <= this.selected) {
            this.selected = 0;
        }

        return this.selected;
    }

    /**
     * Selects the {@link BoxIngredientItem} at the specified position.
     *
     * @param num the position
     */
    public void select(int num) {
        this.selected = num;

        if (this.sortedPatterns.size() <= this.selected) {
            this.selected = 0;
        }
    }

    /**
     * Returns the {@link IngredientHolder#patterns()}.
     *
     * @return the {@link IngredientHolder#patterns()}
     */
    public @NotNull @Unmodifiable List<BoxIngredientItem> get() {
        return this.sortedPatterns;
    }

    /**
     * Returns the number of {@link BoxIngredientItem}s.
     *
     * @return the number of {@link BoxIngredientItem}s
     */
    public int size() {
        return this.sortedPatterns.size();
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

    public void sortPatterns(@Nullable Comparator<BoxIngredientItem> sorter) {
        if (!(this.sortedPatterns instanceof ArrayList)) {
            if (sorter == null) {
                return;
            } else {
                this.sortedPatterns = new ArrayList<>(this.ingredientHolder.patterns());
            }
        }

        if (sorter == null) {
            for (int i = 0, size = this.ingredientHolder.patterns().size(); i < size; i++) {
                this.sortedPatterns.set(i, this.ingredientHolder.patterns().get(i));
            }
        } else {
            this.sortedPatterns.sort(sorter);
        }
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
