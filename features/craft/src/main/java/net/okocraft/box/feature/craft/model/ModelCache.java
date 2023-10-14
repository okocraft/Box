package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class ModelCache {

    private static Map<BoxItem, BoxIngredientItem> GENERATED_INGREDIENT_ITEMS = null;
    private static Map<IngredientHolder, IngredientHolder> INGREDIENT_HOLDER_CACHE = null;

    static void createCache() {
        GENERATED_INGREDIENT_ITEMS = new HashMap<>(100);
        INGREDIENT_HOLDER_CACHE = new HashMap<>(300);
    }

    static void clearCache() {
        GENERATED_INGREDIENT_ITEMS = null;
        INGREDIENT_HOLDER_CACHE = null;
    }

    static @NotNull BoxIngredientItem getIngredientItem(@NotNull BoxItem item, int amount) {
        return GENERATED_INGREDIENT_ITEMS != null && amount == 1 ?
                GENERATED_INGREDIENT_ITEMS.computeIfAbsent(item, ModelCache::createIngredientItem) :
                new BoxIngredientItem(item, amount);
    }

    static @NotNull IngredientHolder getIngredientHolder(int slot, @NotNull List<ItemStack> patterns) {
        var holder = new IngredientHolder(slot, patterns);
        return INGREDIENT_HOLDER_CACHE != null ?
                INGREDIENT_HOLDER_CACHE.computeIfAbsent(holder, Function.identity()) :
                holder;
    }

    private static @NotNull BoxIngredientItem createIngredientItem(@NotNull BoxItem item) {
        return new BoxIngredientItem(item, 1);
    }

    private ModelCache() {
        throw new UnsupportedOperationException();
    }
}
