package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * An interface that is used for calling {@link StockEvent}s from {@link StockHolder}.
 */
public interface StockEventCaller {

    /**
     * A default implementation of {@link  StockEventCaller}.
     * <p>
     * {@link StockEvent}s will be called by {@link com.github.siroshun09.event4j.bus.EventBus#callEventAsync(Object)}.
     */
    @SuppressWarnings("unused")
    StockEventCaller DEFAULT = new DefaultStockEventCallers.Default();

    /**
     * Creates a {@link StockEventCaller} that passes a specified {@link StockHolderWrapper} to {@link StockEvent}.
     *
     * @param wrapper a {@link StockHolderWrapper} that is passed to {@link StockEvent}
     * @return a new {@link StockEventCaller}
     */
    @SuppressWarnings("unused")
    @Contract(value = "_ -> new", pure = true)
    static @NotNull StockEventCaller useWrapper(@NotNull StockHolderWrapper wrapper) {
        return new DefaultStockEventCallers.UsingWrapper(wrapper);
    }

    /**
     * Calls {@link net.okocraft.box.api.event.stockholder.stock.StockSetEvent}.
     *
     * @param stockHolder    the source {@link StockHolder}
     * @param item           the item that has set the amount of stock
     * @param amount         the amount of stock
     * @param previousAmount the amount of stock before set
     * @param cause          the {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that is passed to {@link StockHolder#setAmount(BoxItem, int, StockEvent.Cause)}
     */
    void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, @NotNull StockEvent.Cause cause);

    /**
     * Calls {@link net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent}.
     *
     * @param stockHolder   the source {@link StockHolder}
     * @param item          the item that has set the amount of stock
     * @param increments    the amount of increase
     * @param currentAmount the amount of stock after increase
     * @param cause         the {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that is passed to {@link StockHolder#increase(BoxItem, int, StockEvent.Cause)}
     */
    void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, @NotNull StockEvent.Cause cause);

    /**
     * Calls {@link net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent}.
     *
     * @param stockHolder the source {@link StockHolder}
     * @param item        the item that has set the amount of stock
     * @param increments  the amount of increase (excess is not included)
     * @param excess      the amount exceeding {@link Integer#MAX_VALUE}
     * @param cause       the {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that is passed to {@link StockHolder#increase(BoxItem, int, StockEvent.Cause)}
     */
    void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, @NotNull StockEvent.Cause cause);

    /**
     * Calls {@link net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent}.
     *
     * @param stockHolder   the source {@link StockHolder}
     * @param item          the item that has set the amount of stock
     * @param decrements    the amount of decrease
     * @param currentAmount the amount of stock after decrease
     * @param cause         the {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that is passed to {@link StockHolder#decrease(BoxItem, int, StockEvent.Cause)} or such methods
     */
    void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, @NotNull StockEvent.Cause cause);

    /**
     * Calls {@link net.okocraft.box.api.event.stockholder.StockHolderResetEvent}.
     *
     * @param stockHolder          the source {@link StockHolder}
     * @param stockDataBeforeReset the collection of {@link StockData} before reset
     */
    void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset);

}
