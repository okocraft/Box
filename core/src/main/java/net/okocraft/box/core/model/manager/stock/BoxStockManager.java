package net.okocraft.box.core.model.manager.stock;

import com.github.siroshun09.event4j.bus.EventBus;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.okocraft.box.api.event.BoxEvent;
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
import net.okocraft.box.core.model.manager.stock.autosave.ChangeState;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class BoxStockManager implements StockManager {

    private static final long MILLISECONDS_TO_UNLOAD = Duration.ofMinutes(10).toMillis();

    private final StockStorage stockStorage;
    private final EventBus<BoxEvent> eventBus;
    private final IntFunction<BoxItem> toBoxItem;
    private final Predicate<UUID> onlineChecker;
    private final ChangeState.Factory changeStateFactory;

    private final Object2ReferenceMap<UUID, LoadingPersonalStockHolder> loaderMap = Object2ReferenceMaps.synchronize(new Object2ReferenceOpenHashMap<>());
    private final ConcurrentLinkedQueue<ChangeState> changeStates = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean autoSaveTaskScheduled = new AtomicBoolean(false);

    public BoxStockManager(@NotNull StockStorage stockStorage, @NotNull EventBus<BoxEvent> eventBus, @NotNull IntFunction<BoxItem> toBoxItem, @NotNull Predicate<UUID> onlineChecker) {
        this.stockStorage = stockStorage;
        this.eventBus = eventBus;
        this.toBoxItem = toBoxItem;
        this.onlineChecker = onlineChecker;
        this.changeStateFactory = ChangeState.createFactory(stockStorage, this::logStockStorageError);
    }

    @Override
    public @NotNull LoadingPersonalStockHolder getPersonalStockHolder(@NotNull BoxUser user) {
        return this.loaderMap.computeIfAbsent(user.getUUID(), ignored -> this.createLoader(user));
    }

    private @NotNull LoadingPersonalStockHolder createLoader(@NotNull BoxUser user) {
        return new LoadingPersonalStockHolder(user, this::loadStockHolder);
    }

    private @NotNull StockHolder loadStockHolder(@NotNull LoadingPersonalStockHolder loader) {
        var user = loader.getUser();

        Collection<StockData> stockData;

        try {
            stockData = this.stockStorage.loadStockData(user.getUUID());
        } catch (Exception e) {
            throw new RuntimeException("Could not load user's stock holder (" + user.getUUID() + ")", e);
        }

        var eventCaller = new QueuingStockEventCaller(loader, this.eventBus);
        var stockHolder = StockHolderFactory.create(user, eventCaller, stockData, this.toBoxItem);
        var state = this.changeStateFactory.create(stockHolder);

        eventCaller.state = state;

        this.changeStates.offer(state);

        this.eventBus.callEventAsync(new StockHolderLoadEvent(loader));

        return stockHolder;
    }

    @Override
    public @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller) {
        return this.createStockHolder(uuid, name, eventCaller, Collections.emptyList());
    }

    @Override
    public @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData) {
        return StockHolderFactory.create(uuid, name, eventCaller, stockData, this.toBoxItem);
    }

    public void schedulerAutoSaveTask(@NotNull BoxScheduler scheduler) {
        this.autoSaveTaskScheduled.set(true);
        scheduler.scheduleRepeatingAsyncTask(this::saveChangesAndCleanupOffline, Duration.ofMinutes(5), this.autoSaveTaskScheduled::get);
    }

    public void close() {
        this.autoSaveTaskScheduled.set(false);

        ChangeState state;

        while ((state = this.changeStates.poll()) != null) {
            state.saveChanges();

            var removedLoader = this.loaderMap.remove(state.getStockHolder().getUUID());

            if (removedLoader != null) {
                removedLoader.unload();
            }
        }

        if (this.stockStorage instanceof PartialSavingStockStorage partialSavingStockStorage) {
            try {
                partialSavingStockStorage.cleanupZeroStockData();
            } catch (Exception e) {
                BoxLogger.logger().error("Could not cleanup stock data", e);
            }
        }

        this.loaderMap.clear();
    }

    private void saveChangesAndCleanupOffline() {
        ChangeState state;
        ChangeState firstRestoredState = null;

        while ((state = this.changeStates.poll()) != null) {
            if (state == firstRestoredState) {
                this.changeStates.offer(state);
                break;
            }

            state.saveChanges();

            var uuid = state.getStockHolder().getUUID();
            var loader = this.loaderMap.get(uuid);

            if (loader == null) {
                continue;
            }

            this.eventBus.callEventAsync(new StockHolderSaveEvent(loader));

            if (this.onlineChecker.test(uuid) || !loader.unloadIfNeeded(MILLISECONDS_TO_UNLOAD)) {
                this.changeStates.offer(state);

                if (firstRestoredState == null) {
                    firstRestoredState = state;
                }
            }
        }
    }

    private void logStockStorageError(@NotNull StockHolder stockHolder, @NotNull Exception e) {
        BoxLogger.logger().error("Could not save user's stock holder (name: {} uuid: {})", stockHolder.getName(), stockHolder.getUUID(), e);
    }

    private static class QueuingStockEventCaller implements StockEventCaller {

        private final LoadingPersonalStockHolder loader;
        private final EventBus<BoxEvent> eventBus;
        private ChangeState state; // initialize later

        private QueuingStockEventCaller(@NotNull LoadingPersonalStockHolder loader, @NotNull EventBus<BoxEvent> eventBus) {
            this.loader = loader;
            this.eventBus = eventBus;
        }

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
            this.callEvent(new StockSetEvent(this.loader, item, amount, previousAmount, cause));
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
            this.callEvent(new StockIncreaseEvent(this.loader, item, increments, currentAmount, cause));
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
            this.callEvent(new StockOverflowEvent(this.loader, item, increments, excess, cause));
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
            this.callEvent(new StockDecreaseEvent(this.loader, item, decrements, currentAmount, cause));
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
            this.state.rememberReset(stockDataBeforeReset);
            this.callEvent(new StockHolderResetEvent(this.loader, stockDataBeforeReset));
        }

        private void callEvent(@NotNull BoxEvent event) {
            this.eventBus.callEventAsync(event);
        }
    }
}
