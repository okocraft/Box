package net.okocraft.box.feature.craft.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation indicating that the amount of stock has been modified by crafting.
 *
 * @param crafter        the {@link Player} who craft items
 * @param selectedRecipe the {@link SelectedRecipe} used to craft items
 * @param actor          the crafter's captured identity
 */
public record CraftCause(@NotNull Player crafter, @NotNull SelectedRecipe selectedRecipe,
                         StockActor.@NotNull Player actor) implements StockEvent.Cause {
    public CraftCause {
        Objects.requireNonNull(actor, "actor");
    }

    public CraftCause(@NotNull Player crafter, @NotNull SelectedRecipe selectedRecipe) {
        this(crafter, selectedRecipe, StockActor.fromPlayer(crafter));
    }

    @Override
    public @NotNull String name() {
        return "craft";
    }
}
