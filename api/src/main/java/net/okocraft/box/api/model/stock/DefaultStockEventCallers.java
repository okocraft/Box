package net.okocraft.box.api.model.stock;

import com.github.siroshun09.event4j.caller.AsyncEventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

final class DefaultStockEventCallers {

    static final class Default implements StockEventCaller {

        private final AsyncEventCaller<BoxEvent> eventCaller;

        Default(@NotNull AsyncEventCaller<BoxEvent> eventCaller) {
            this.eventCaller = eventCaller;
        }

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockSetEvent(stockHolder, item, amount, previousAmount, cause));
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockIncreaseEvent(stockHolder, item, increments, currentAmount, cause));
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockOverflowEvent(stockHolder, item, increments, excess, cause));
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockDecreaseEvent(stockHolder, item, decrements, currentAmount, cause));
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
            this.callEvent(new StockHolderResetEvent(stockHolder, stockDataBeforeReset));
        }

        private void callEvent(@NotNull BoxEvent event) {
            this.eventCaller.callAsync(event);
        }
    }

    static final class UsingWrapper implements StockEventCaller {

        private final AsyncEventCaller<BoxEvent> eventCaller;
        private final StockHolderWrapper wrapper;

        UsingWrapper(@NotNull AsyncEventCaller<BoxEvent> eventCaller, @NotNull StockHolderWrapper wrapper) {
            this.eventCaller = eventCaller;
            this.wrapper = Objects.requireNonNull(wrapper);
        }

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockSetEvent(this.wrapper, item, amount, previousAmount, cause));
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockIncreaseEvent(this.wrapper, item, increments, currentAmount, cause));
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockOverflowEvent(this.wrapper, item, increments, excess, cause));
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.callEvent(new StockDecreaseEvent(this.wrapper, item, decrements, currentAmount, cause));
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
            this.callEvent(new StockHolderResetEvent(this.wrapper, stockDataBeforeReset));
        }

        private void callEvent(@NotNull BoxEvent event) {
            this.eventCaller.callAsync(event);
        }
    }

}
