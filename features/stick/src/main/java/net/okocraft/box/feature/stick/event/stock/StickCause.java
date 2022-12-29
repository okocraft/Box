package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

public record StickCause(@NotNull BoxPlayer player) implements StockEvent.Cause {
    @Override
    public @NotNull String name() {
        return "stick";
    }
}
