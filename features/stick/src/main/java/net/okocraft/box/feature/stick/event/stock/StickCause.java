package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

public interface StickCause extends StockEvent.Cause {

    @NotNull BoxPlayer player();

}
