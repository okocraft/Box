package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class VoidStockEventCaller implements StockEventCaller {

    public static final VoidStockEventCaller INSTANCE = new VoidStockEventCaller();

    private VoidStockEventCaller() {
    }

    @Override
    public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
    }

    @Override
    public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
    }

    @Override
    public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
    }

    @Override
    public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
    }

    @Override
    public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
    }
}
