package net.okocraft.box.feature.craft.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record CraftCause(@NotNull Player crafter, @NotNull SelectedRecipe selectedRecipe) implements StockEvent.Cause {

    @Override
    public @NotNull String name() {
        return "craft";
    }
}
