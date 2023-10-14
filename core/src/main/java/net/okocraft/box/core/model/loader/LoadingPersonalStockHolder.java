package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class LoadingPersonalStockHolder implements PersonalStockHolder {

    private final BoxUser user;
    private final Function<LoadingPersonalStockHolder, StockHolder> loader;

    private volatile StockHolder stockHolder;
    private volatile long lastAccess = Long.MIN_VALUE;

    public LoadingPersonalStockHolder(@NotNull BoxUser user, @NotNull Function<LoadingPersonalStockHolder, StockHolder> loader) {
        this.user = user;
        this.loader = loader;
    }

    @Override
    public @NotNull BoxUser getUser() {
        return user;
    }

    @Override
    public @NotNull StockHolder delegate() {
        this.lastAccess = System.currentTimeMillis();
        var stockHolder = this.stockHolder;
        return stockHolder != null ? stockHolder : load();
    }

    public @NotNull StockHolder load() {
        StockHolder stockHolder;

        synchronized (this) {
            this.lastAccess = System.currentTimeMillis();
            if (this.stockHolder == null) {
                this.stockHolder = this.loader.apply(this);
            }
            stockHolder = this.stockHolder;
        }

        return stockHolder;
    }

    public void unload() {
        synchronized (this) {
            this.stockHolder = null;
            this.lastAccess = Long.MIN_VALUE;
        }
    }

    public boolean unloadIfNeeded(long millisecondsToUnload) {
        if (!shouldUnload(millisecondsToUnload)) { // Ensure that unloading is required before locking.
            return false;
        }

        boolean result;

        synchronized (this) {
            if (shouldUnload(millisecondsToUnload)) {
                if (this.stockHolder != null) {
                    this.stockHolder = null;
                }
                this.lastAccess = Long.MIN_VALUE;
                result = true;
            } else {
                result = false;
            }
        }

        return result;
    }

    public boolean shouldUnload(long millisecondsToUnload) {
        long lastAccess = this.lastAccess;
        return lastAccess != Long.MIN_VALUE && System.currentTimeMillis() - lastAccess > millisecondsToUnload;
    }

    @Override
    public String toString() {
        return "LoadingStockHolder{" +
                "user=" + user +
                ", isLoaded=" + (stockHolder != null) +
                ", lastAccess=" + lastAccess +
                '}';
    }
}
