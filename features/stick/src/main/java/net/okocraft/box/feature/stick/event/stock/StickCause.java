package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation indicating that the amount of stock has been modified by Box Stick.
 *
 * @param player the {@link BoxPlayer} who use the Box Stick
 */
public record StickCause(@NotNull BoxPlayer player) implements StockEvent.Cause {
    @Override
    public @NotNull String name() {
        return "stick";
    }
}
