package net.okocraft.box.api.event.player;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PlayerEvent} called when the player changed the {@link StockHolder}.
 */
public class PlayerStockHolderChangeEvent extends PlayerEvent {

    private final StockHolder previous;

    /**
     * The constructor of a {@link PlayerStockHolderChangeEvent}.
     *
     * @param boxPlayer the player of this event
     * @param previous the previous {@link StockHolder}
     */
    public PlayerStockHolderChangeEvent(@NotNull BoxPlayer boxPlayer,
                                        @NotNull StockHolder previous) {
        super(boxPlayer);
        this.previous = previous;
    }

    /**
     * Gets the previous {@link StockHolder} of the player.
     *
     * @return the previous {@link StockHolder} of the player
     */
    public @NotNull StockHolder getPreviousStockHolder() {
        return previous;
    }
}
