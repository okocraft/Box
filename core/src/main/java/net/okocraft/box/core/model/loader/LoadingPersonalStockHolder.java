package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.loader.state.ChangeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.UUID;
import java.util.function.Function;

public class LoadingPersonalStockHolder implements PersonalStockHolder {

    private static final long LAST_ACCESS_ONLINE = -1;
    private static final long LAST_ACCESS_ZERO = 0;

    private final BoxUser user;
    private final ChangeState changeState;
    private final Function<LoadingPersonalStockHolder, StockHolder> loader;

    private volatile StockHolder stockHolder;
    private volatile long lastAccess = LAST_ACCESS_ZERO;
    private volatile boolean closed;

    public LoadingPersonalStockHolder(@NotNull BoxUser user, @NotNull ChangeState changeState, @NotNull Function<LoadingPersonalStockHolder, StockHolder> loader) {
        this.user = user;
        this.changeState = changeState;
        this.loader = loader;
    }

    @Override
    public @NotNull BoxUser getUser() {
        return this.user;
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.user.getUUID();
    }

    @Override
    public @NotNull String getName() {
        return this.user.getName().orElse("Unknown");
    }

    @Override
    public @NotNull StockHolder delegate() {
        if (this.closed) {
            throw new IllegalStateException("This loader is no longer available.");
        }

        if (this.lastAccess != LAST_ACCESS_ONLINE) {
            this.lastAccess = System.nanoTime();
        }

        var stockHolder = this.stockHolder;
        return stockHolder != null ? stockHolder : this.load();
    }

    public @NotNull StockHolder load() {
        if (this.closed) {
            throw new IllegalStateException("This loader is no longer available.");
        }

        StockHolder stockHolder;

        synchronized (this) {
            if (this.stockHolder == null) {
                this.stockHolder = this.loader.apply(this);
            }
            stockHolder = this.stockHolder;
        }

        return stockHolder;
    }

    public @Nullable StockHolder close() {
        this.closed = true;

        StockHolder unloaded;

        synchronized (this) {
            unloaded = this.stockHolder;
            this.stockHolder = null;
        }

        return unloaded;
    }

    public void saveChangesOrUnloadIfNeeded(long unloadTimeInNanos, long saveIntervalInNanos) throws Exception {
        if (this.closed || this.stockHolder == null) {
            return;
        }

        boolean unloaded = this.shouldUnload(unloadTimeInNanos) && this.unloadIfNeeded(unloadTimeInNanos); // checks before locking

        if (unloaded || this.changeState.isInInterval(saveIntervalInNanos)) {
            return;
        }

        var loaded = this.stockHolder;

        if (loaded != null) {
            this.changeState.saveChanges(loaded);
        }
    }

    private boolean unloadIfNeeded(long unloadTimeInNanos) throws Exception {
        synchronized (this) {
            if (this.shouldUnload(unloadTimeInNanos)) {
                if (this.stockHolder != null) {
                    var unloading = this.stockHolder;
                    this.stockHolder = null;

                    this.changeState.saveChanges(unloading); // Save here to prevent loading before applying data to the storage
                    this.lastAccess = LAST_ACCESS_ZERO;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldUnload(long nanosToUnload) {
        long lastAccess = this.lastAccess;
        return lastAccess != LAST_ACCESS_ONLINE && System.nanoTime() - lastAccess > nanosToUnload;
    }

    public void markAsOnline() {
        this.lastAccess = LAST_ACCESS_ONLINE;
    }

    public void markAsOffline() {
        this.lastAccess = System.nanoTime();
    }

    public @NotNull ChangeState getChangeState() {
        return this.changeState;
    }

    @VisibleForTesting
    boolean isLoaded() {
        return this.stockHolder != null;
    }

    @VisibleForTesting
    boolean isClosed() {
        return this.closed;
    }

    @Override
    public String toString() {
        return "LoadingStockHolder{" +
                "user=" + this.user +
                ", isLoaded=" + this.isLoaded() +
                ", isClosed=" + this.isClosed() +
                ", lastAccess=" + this.lastAccess +
                '}';
    }
}
