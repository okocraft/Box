package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.BoxAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class that holds multiple {@link BoxIngredientItem}s.
 */
public class IngredientHolder {

    /**
     * Enables caching objects.
     */
    @ApiStatus.Internal
    public static void enableCache() {
        ModelCache.createCache();
    }

    /**
     * Disables caching objects.
     */
    @ApiStatus.Internal
    public static void disableCache() {
        ModelCache.clearCache();
    }

    /**
     * Creates a {@link IngredientHolder} from {@link org.bukkit.inventory.RecipeChoice.MaterialChoice}.
     *
     * @param slot   the position in the crafting table (0-8)
     * @param choice the {@link org.bukkit.inventory.RecipeChoice.MaterialChoice} instance to create {@link IngredientHolder}
     * @return a {@link IngredientHolder}
     */
    public static @NotNull IngredientHolder fromMaterialChoice(int slot, @NotNull RecipeChoice.MaterialChoice choice) {
        return ModelCache.getIngredientHolder(slot, choice.getChoices().stream().map(ItemStack::new).toList());
    }

    /**
     * Creates a {@link IngredientHolder} from {@link org.bukkit.inventory.RecipeChoice.ExactChoice}.
     *
     * @param slot   the position in the crafting table (0-8)
     * @param choice the {@link org.bukkit.inventory.RecipeChoice.ExactChoice} instance to create {@link IngredientHolder}
     * @return a {@link IngredientHolder}
     */
    public static @NotNull IngredientHolder fromExactChoice(int slot, @NotNull RecipeChoice.ExactChoice choice) {
        return ModelCache.getIngredientHolder(slot, choice.getChoices());
    }

    /**
     * Creates a {@link IngredientHolder} from the single {@link ItemStack}.
     *
     * @param slot      the position in the crafting table (0-8)
     * @param itemStack the {@link ItemStack} to create {@link IngredientHolder}
     * @return a {@link IngredientHolder}
     */
    public static @NotNull IngredientHolder fromSingleItem(int slot, @NotNull ItemStack itemStack) {
        return ModelCache.getIngredientHolder(slot, List.of(itemStack));
    }

    private final int slot;
    private final List<BoxIngredientItem> patterns;

    /**
     * Creates a {@link IngredientHolder}.
     *
     * @param slot     the position in the crafting table (0-8)
     * @param patterns the {@link ItemStack} list to include
     */
    @ApiStatus.Internal
    public IngredientHolder(int slot, @NotNull List<ItemStack> patterns) {
        this.slot = slot;

        var tempPattern = new ArrayList<BoxIngredientItem>(patterns.size());

        for (var item : patterns) {
            BoxAPI.api().getItemManager().getBoxItem(item)
                    .ifPresent(value -> tempPattern.add(ModelCache.getIngredientItem(value, item.getAmount())));
        }

        this.patterns = Collections.unmodifiableList(tempPattern);
    }

    /**
     * Returns the position in the crafting table (0-8).
     * <p>
     * A 3x3 craft table is represented by the following numbers:
     * <p>
     * <code>0 1 2</code>
     * <br>
     * <code>3 4 5</code>
     * <br>
     * <code>6 7 8</code>
     *
     * @return the position in the crafting table (0-8)
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Returns the {@link BoxIngredientItem} list that is contained this {@link IngredientHolder}.
     *
     * @return the {@link BoxIngredientItem} list that is contained this {@link IngredientHolder}
     */
    public @NotNull @Unmodifiable List<BoxIngredientItem> getPatterns() {
        return patterns;
    }

    /**
     * Creates a {@link SelectableIngredients} from {@link #getPatterns()}.
     *
     * @return a {@link SelectableIngredients}
     */
    public @NotNull SelectableIngredients toSelectableIngredients() {
        return new SelectableIngredients(patterns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientHolder that = (IngredientHolder) o;

        if (slot != that.slot) {
            return false;
        }

        if (patterns.size() != that.patterns.size()) {
            return false;
        }

        for (var pattern : patterns) {
            if (!that.patterns.contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, patterns);
    }

    /**
     * A class for handling multiple {@link BoxIngredientItem}s that can be selected
     */
    public static class SelectableIngredients {

        private final List<BoxIngredientItem> patterns;

        private int selected;

        private SelectableIngredients(@NotNull List<BoxIngredientItem> patterns) {
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
         * Returns the {@link IngredientHolder#getPatterns()}.
         *
         * @return the {@link IngredientHolder#getPatterns()}
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
            if (patterns.size() != other.patterns.size()) {
                return false;
            }

            for (var pattern : patterns) {
                if (!other.patterns.contains(pattern)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectableIngredients that = (SelectableIngredients) o;
            return isSameIngredient(that);
        }

        @Override
        public int hashCode() {
            return Objects.hash(patterns, selected);
        }
    }
}
