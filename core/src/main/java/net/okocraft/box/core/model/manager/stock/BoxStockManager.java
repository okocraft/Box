package net.okocraft.box.core.model.manager.stock;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.event.stockholder.StockHolderLoadEvent;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.core.model.loader.LoadingPersonalStockHolder;
import net.okocraft.box.core.model.loader.state.ChangeState;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class BoxStockManager implements StockManager {

    private final StockStorage stockStorage;
    private final EventCallerProvider eventCallers;
    private final IntFunction<BoxItem> toBoxItem;
    private final Supplier<ChangeState> changeStateFactory;
    private final long unloadTime;
    private final long saveInterval;

    private final ConcurrentMap<UUID, LoadingPersonalStockHolder> loaderMap = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public BoxStockManager(@NotNull StockStorage stockStorage,
                           @NotNull EventCallerProvider eventCallers,
                           @NotNull IntFunction<BoxItem> toBoxItem,
                           long unloadTime, long saveInterval, @NotNull TimeUnit timeUnit) {
        this.stockStorage = stockStorage;
        this.eventCallers = eventCallers;
        this.toBoxItem = toBoxItem;
        this.changeStateFactory = ChangeState.createSupplier(stockStorage);
        this.unloadTime = Math.max(0, timeUnit.toNanos(unloadTime));
        this.saveInterval = Math.max(0, timeUnit.toNanos(saveInterval));
    }

    @Override
    public @NotNull LoadingPersonalStockHolder getPersonalStockHolder(@NotNull BoxUser user) {
        this.checkClosed();
        return this.loaderMap.computeIfAbsent(user.getUUID(), ignored -> this.createLoader(user));
    }

    private @NotNull LoadingPersonalStockHolder createLoader(@NotNull BoxUser user) {
        return new LoadingPersonalStockHolder(user, this.changeStateFactory.get(), this::loadStockHolder);
    }

    private @NotNull StockHolder loadStockHolder(@NotNull LoadingPersonalStockHolder loader) {
        this.checkClosed();

        var user = loader.getUser();

        Collection<StockData> stockData;

        try {
            stockData = this.stockStorage.loadStockData(user.getUUID());
        } catch (Exception e) {
            throw new RuntimeException("Could not load user's stock holder (" + user.getUUID() + ")", e);
        }

        var eventCaller = this.createStockEventCaller(loader);
        var stockHolder = StockHolderFactory.create(user, eventCaller, stockData, this.toBoxItem);

        this.eventCallers.async().call(new StockHolderLoadEvent(loader));

        return stockHolder;
    }

    @Override
    public @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller) {
        return this.createStockHolder(uuid, name, eventCaller, Collections.emptyList());
    }

    @Override
    public @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData) {
        this.checkClosed();
        return StockHolderFactory.create(uuid, name, eventCaller, stockData, this.toBoxItem);
    }

    public void schedulerAutoSaveTask(@NotNull BoxScheduler scheduler) {
        this.checkClosed();
        long interval = computeInterval(this.unloadTime, this.saveInterval);
        scheduler.scheduleRepeatingAsyncTask(this::saveChangesAndCleanupOffline, Duration.ofNanos(interval), this::isNotClosed);
    }

    public void close() {
        if (!this.closed.compareAndSet(false, true)) {
            throw new IllegalStateException("This BoxStockManager is already closed.");
        }

        this.loaderMap.values().forEach(this::closeLoader);
        this.loaderMap.clear();

        if (this.stockStorage instanceof PartialSavingStockStorage storage) {
            try {
                storage.cleanupZeroStockData();
            } catch (Exception e) {
                BoxLogger.logger().error("Failed to clean up stock data.", e);
            }
        }
    }

    private void saveChangesAndCleanupOffline() {
        this.loaderMap.values().forEach(this::autoSaveOrUnload);
    }

    @VisibleForTesting
    static long computeInterval(long unloadTime, long saveInterval) {
        if (unloadTime == 0 && saveInterval == 0) {
            return TimeUnit.SECONDS.toNanos(1);
        } else {
            return BigInteger.valueOf(unloadTime).gcd(BigInteger.valueOf(saveInterval)).longValue();
        }
    }

    @VisibleForTesting
    @NotNull StockEventCaller createStockEventCaller(@NotNull LoadingPersonalStockHolder loader) {
        return new StateUpdatingStockEventCaller(loader, this.eventCallers.async());
    }

    @VisibleForTesting
    void autoSaveOrUnload(@NotNull LoadingPersonalStockHolder loader) {
        if (this.closed.get() || !loader.isLoaded()) {
            return;
        }

        try {
            loader.saveChangesOrUnloadIfNeeded(this.unloadTime, this.saveInterval);
            this.eventCallers.async().call(new StockHolderSaveEvent(loader));
        } catch (Exception e) {
            BoxLogger.logger().error("Could not save user's stock holder (name: {} uuid: {})", loader.getName(), loader.getUUID(), e);
        }
    }

    @VisibleForTesting
    void closeLoader(@NotNull LoadingPersonalStockHolder loader) {
        var unloaded = loader.close();

        if (unloaded != null) {
            try {
                loader.getChangeState().saveChanges(unloaded);
            } catch (Exception e) {
                BoxLogger.logger().error("Could not save user's stock holder before closing (name: {} uuid: {})", loader.getName(), loader.getUUID(), e);
            }
        }
    }

    private boolean isNotClosed() {
        return !this.closed.get();
    }

    private void checkClosed() {
        if (this.closed.get()) {
            throw new IllegalStateException("This BoxStockManager is already closed.");
        }
    }

    private record StateUpdatingStockEventCaller(LoadingPersonalStockHolder loader,
                                                 EventCaller<BoxEvent> caller) implements StockEventCaller {

        private StateUpdatingStockEventCaller(@NotNull LoadingPersonalStockHolder loader, @NotNull EventCaller<BoxEvent> caller) {
            this.loader = loader;
            this.caller = caller;
        }

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
            this.loader.getChangeState().rememberChange(item.getInternalId());
            this.caller.call(new StockSetEvent(this.loader, item, amount, previousAmount, cause));
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.loader.getChangeState().rememberChange(item.getInternalId());
            this.caller.call(new StockIncreaseEvent(this.loader, item, increments, currentAmount, cause));
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
            this.loader.getChangeState().rememberChange(item.getInternalId());
            this.caller.call(new StockOverflowEvent(this.loader, item, increments, excess, cause));
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.loader.getChangeState().rememberChange(item.getInternalId());
            this.caller.call(new StockDecreaseEvent(this.loader, item, decrements, currentAmount, cause));
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
            this.loader.getChangeState().rememberReset(stockDataBeforeReset);
            this.caller.call(new StockHolderResetEvent(this.loader, stockDataBeforeReset));
        }
    }
}
