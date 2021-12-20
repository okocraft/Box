package net.okocraft.box.feature.craft.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RecipeImportEvent extends BoxEvent {

    private final BoxItemRecipe recipe;

    public RecipeImportEvent(@NotNull BoxItemRecipe recipe) {
        this.recipe = Objects.requireNonNull(recipe);
    }

    public @NotNull BoxItemRecipe getRecipe() {
        return recipe;
    }

    @Override
    public String toString() {
        return "RecipeImportEvent{" +
                "recipe=" + recipe +
                '}';
    }
}
