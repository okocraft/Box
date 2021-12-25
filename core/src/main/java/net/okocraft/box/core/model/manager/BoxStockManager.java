package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stockholder.StockHolderLoadEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.loader.UserStockHolderLoader;
import net.okocraft.box.core.model.queue.AutoSaveQueue;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BoxStockManager implements StockManager {

    private final StockStorage stockStorage;
    private final AutoSaveQueue queue;
    private final ExecutorService executor;

    private final Map<BoxUser, UserStockHolderLoader> loaderMap = new HashMap<>();
    private final Map<BoxUser, Object> lockMap = new HashMap<>();

    public BoxStockManager(@NotNull StockStorage stockStorage, @NotNull AutoSaveQueue queue) {
        this.stockStorage = stockStorage;
        this.queue = queue;
        this.executor = InternalExecutors.newSingleThreadExecutor("Stock Manager");
    }

    @Override
    public @NotNull CompletableFuture<@NotNull UserStockHolder> loadUserStock(@NotNull BoxUser user) {
        Objects.requireNonNull(user);

        return CompletableFuture.supplyAsync(() -> {
            if (loaderMap.containsKey(user)) {
                return loaderMap.get(user);
            }

            var loader = new UserStockHolderLoader(user, this::loadUserStockHolder0, queue);
            loaderMap.put(user, loader);
            loader.load();

            return loader;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUserStock(@NotNull UserStockHolder stockHolder) {
        Objects.requireNonNull(stockHolder);

        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (getLock(stockHolder.getUser())) {
                    stockStorage.saveUserStockHolder(stockHolder);
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not save user stock holder (" + stockHolder.getUser().getUUID() + ")", e);
            }
            BoxProvider.get().getEventBus().callEvent(new StockHolderSaveEvent(stockHolder));
        }, executor);
    }

    private @NotNull UserStockHolder loadUserStockHolder0(@NotNull BoxUser user) {
        UserStockHolder stockHolder;

        try {
            synchronized (getLock(user)) {
                stockHolder = stockStorage.loadUserStockHolder(user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load user stock holder (" + user.getUUID() + ")", e);
        }

        BoxProvider.get().getEventBus().callEvent(new StockHolderLoadEvent(stockHolder));
        return stockHolder;
    }

    private @NotNull Object getLock(@NotNull BoxUser user) {
        return lockMap.computeIfAbsent(user, u -> new Object());
    }
}
