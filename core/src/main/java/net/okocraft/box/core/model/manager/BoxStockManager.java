package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stockholder.StockHolderLoadEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BoxStockManager implements StockManager {

    private final StockStorage stockStorage;
    private final ExecutorService executor;

    public BoxStockManager(@NotNull StockStorage stockStorage) {
        this.stockStorage = stockStorage;
        this.executor = InternalExecutors.newSingleThreadExecutor("Stock Manager");
    }

    @Override
    public @NotNull CompletableFuture<@NotNull UserStockHolder> loadUserStock(@NotNull BoxUser user) {
        Objects.requireNonNull(user);

        return CompletableFuture.supplyAsync(() -> {
            UserStockHolder stockHolder;
            try {
                stockHolder = stockStorage.loadUserStockHolder(user);
            } catch (Exception e) {
                throw new RuntimeException("Could not load user stock holder (" + user.getUUID() + ")", e);
            }

            BoxProvider.get().getEventBus().callEvent(new StockHolderLoadEvent(stockHolder));
            return stockHolder;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUserStock(@NotNull UserStockHolder stockHolder) {
        Objects.requireNonNull(stockHolder);

        return CompletableFuture.runAsync(() -> {
            try {
                stockStorage.saveUserStockHolder(stockHolder);
                BoxProvider.get().getEventBus().callEvent(new StockHolderSaveEvent(stockHolder));
            } catch (Exception e) {
                throw new RuntimeException("Could not save user stock holder (" + stockHolder.getUser().getUUID() + ")", e);
            }
        }, executor);
    }
}
