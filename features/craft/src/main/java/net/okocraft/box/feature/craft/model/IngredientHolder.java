package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.BoxProvider;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IngredientHolder {

    public static @NotNull IngredientHolder fromMaterialChoice(int slot, @NotNull RecipeChoice.MaterialChoice choice) {
        return new IngredientHolder(slot, choice.getChoices().stream().map(ItemStack::new).toList());
    }

    public static @NotNull IngredientHolder fromExactChoice(int slot, @NotNull RecipeChoice.ExactChoice choice) {
        return new IngredientHolder(slot, choice.getChoices());
    }

    public static @NotNull IngredientHolder fromSingleItem(int slot, @NotNull ItemStack itemStack) {
        return new IngredientHolder(slot, List.of(itemStack));
    }

    private final int slot;
    private final List<BoxIngredientItem> patterns;

    private IngredientHolder(int slot, @NotNull List<ItemStack> patterns) {
        this.slot = slot;

        var tempPattern = new ArrayList<BoxIngredientItem>();

        for (var item : patterns) {
            var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);
            boxItem.ifPresent(value -> tempPattern.add(new BoxIngredientItem(value, item.getAmount())));
        }

        this.patterns = Collections.unmodifiableList(tempPattern);
    }

    public int getSlot() {
        return slot;
    }

    public @NotNull @Unmodifiable List<BoxIngredientItem> getPatterns() {
        return patterns;
    }

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

    public static class SelectableIngredients {

        private final List<BoxIngredientItem> patterns;

        private int selected;

        private SelectableIngredients(@NotNull List<BoxIngredientItem> patterns) {
            this.patterns = patterns;
        }

        public @NotNull BoxIngredientItem getSelected() {
            return patterns.get(selected);
        }

        public int next() {
            selected++;

            if (patterns.size() <= selected) {
                selected = 0;
            }

            return selected;
        }

        public void select(int num) {
            selected = num;

            if (patterns.size() <= selected) {
                selected = 0;
            }

            getSelected();
        }

        public @NotNull @Unmodifiable List<BoxIngredientItem> get() {
            return patterns;
        }

        public int size() {
            return patterns.size();
        }

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
