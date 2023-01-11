package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that represents Box Stick related {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause}.
 */
public interface StickCause extends StockEvent.Cause {

    /**
     * Returns the {@link BoxPlayer} that used the Box Stick.
     *
     * @return the {@link BoxPlayer} that used the Box Stick
     */
    @NotNull BoxPlayer player();

}
