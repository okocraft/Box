package net.okocraft.box.feature.craft.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A class that holds multiple {@link BoxIngredientItem}s.
 *
 * @param slot     the position in the crafting table (0-8)
 * @param patterns the {@link BoxIngredientItem} list that is contained this {@link IngredientHolder}
 */
public record IngredientHolder(int slot, @NotNull @Unmodifiable List<BoxIngredientItem> patterns) {

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

    public static @NotNull IngredientHolder create(int slot, @NotNull @Unmodifiable List<BoxIngredientItem> patterns) {
        return ModelCache.getIngredientHolder(new IngredientHolder(slot, patterns));
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
    @Override
    public int slot() {
        return this.slot;
    }
}
