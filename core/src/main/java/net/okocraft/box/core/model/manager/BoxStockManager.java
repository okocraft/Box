package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BoxStockManager implements StockManager {

    private final StockStorage stockStorage;
    private final ExecutorService executor;

    public BoxStockManager(@NotNull StockStorage stockStorage) {
        this.stockStorage = stockStorage;
        this.executor = InternalExecutors.newSingleThreadExecutor("Stock-Manager");
    }

    @Override
    public @NotNull CompletableFuture<@NotNull UserStockHolder> loadUserStock(@NotNull BoxUser user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return stockStorage.loadUserStockHolder(user);
            } catch (Exception e) {
                throw new RuntimeException("Could not load user stock holder (" + user.getUUID() + ")", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUserStock(@NotNull UserStockHolder stockHolder) {
        return CompletableFuture.runAsync(() -> {
            try {
                stockStorage.saveUserStockHolder(stockHolder);
            } catch (Exception e) {
                throw new RuntimeException("Could not save user stock holder (" + stockHolder.getUser().getUUID() + ")", e);
            }
        }, executor);
    }
}
