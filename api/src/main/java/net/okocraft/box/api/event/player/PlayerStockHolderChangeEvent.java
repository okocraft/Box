package net.okocraft.box.api.event.player;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link PlayerEvent} called when the player changed the {@link StockHolder}.
 */
public class PlayerStockHolderChangeEvent extends PlayerEvent {

    private final StockHolder previous;

    /**
     * The constructor of a {@link PlayerStockHolderChangeEvent}.
     *
     * @param boxPlayer the player of this event
     * @param previous  the previous {@link StockHolder}
     */
    public PlayerStockHolderChangeEvent(@NotNull BoxPlayer boxPlayer,
                                        @NotNull StockHolder previous) {
        super(boxPlayer);
        this.previous = Objects.requireNonNull(previous);
    }

    /**
     * Gets the previous {@link StockHolder} of the player.
     *
     * @return the previous {@link StockHolder} of the player
     */
    public @NotNull StockHolder getPreviousStockHolder() {
        return previous;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "PlayerStockHolderChangeEvent{" +
                "uuid=" + getBoxPlayer().getUUID() +
                ", name=" + getBoxPlayer().getName() +
                ", previousStockholderUuid=" + previous.getUUID() +
                ", previousStockHolderName=" + previous.getName() +
                ", previousStockHolderClass=" + previous.getClass().getSimpleName() +
                ", currentStockholderUuid=" + getBoxPlayer().getCurrentStockHolder().getUUID() +
                ", currentStockHolderName=" + getBoxPlayer().getCurrentStockHolder().getName() +
                ", currentStockHolderClass=" + getBoxPlayer().getCurrentStockHolder().getClass().getSimpleName() +
                '}';
    }

    @Override
    public String toString() {
        return "PlayerStockHolderChangeEvent{" +
                "boxPlayer=" + getBoxPlayer() +
                ", previous=" + previous +
                '}';
    }
}
