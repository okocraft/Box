package net.okocraft.box.feature.craft.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An event that called when the {@link BoxItemRecipe} is imported.
 */
public class RecipeImportEvent extends BoxEvent {

    private final BoxItemRecipe recipe;

    /**
     * The constructor of {@link RecipeImportEvent}.
     *
     * @param recipe the imported {@link BoxItemRecipe}
     */
    public RecipeImportEvent(@NotNull BoxItemRecipe recipe) {
        this.recipe = Objects.requireNonNull(recipe);
    }

    /**
     * Gets the imported {@link BoxItemRecipe}.
     *
     * @return the imported {@link BoxItemRecipe}
     */
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
