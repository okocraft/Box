package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.queue.AutoSaveQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

public class UserStockHolderLoader implements UserStockHolder {

    private final BoxUser user;
    private final Function<BoxUser, UserStockHolder> loader;
    private final AutoSaveQueue queue;

    private final AtomicReference<UserStockHolder> loadedStockHolderReference = new AtomicReference<>();
    private final Object lock = new Object();

    public UserStockHolderLoader(@NotNull BoxUser user,
                                 @NotNull Function<BoxUser, UserStockHolder> loader,
                                 @NotNull AutoSaveQueue queue) {
        this.user = user;
        this.loader = loader;
        this.queue = queue;
    }

    @Override
    public @NotNull String getName() {
        return user.getName().orElse("Unknown");
    }

    @Override
    public @NotNull UUID getUUID() {
        return user.getUUID();
    }

    @Override
    public @NotNull BoxUser getUser() {
        return user;
    }

    @Override
    public int getAmount(@NotNull BoxItem item) {
        return checkAndGetUserStockHolder().getAmount(item);
    }

    @Override
    public void setAmount(@NotNull BoxItem item, int amount, @NotNull StockEvent.Cause cause) {
        checkAndGetUserStockHolder().setAmount(item, amount, cause);
        queue.enqueue(this);
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment, @NotNull StockEvent.Cause cause) {
        int current = checkAndGetUserStockHolder().increase(item, increment, cause);
        queue.enqueue(this);
        return current;
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
        int current = checkAndGetUserStockHolder().decrease(item, decrement, cause);
        queue.enqueue(this);
        return current;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
        return checkAndGetUserStockHolder().getStockedItems();
    }

    @Override
    public @NotNull @Unmodifiable Collection<StockData> toStockDataCollection() {
        return checkAndGetUserStockHolder().toStockDataCollection();
    }

    @Override
    public @NotNull Stream<StockData> stockDataStream() {
        return checkAndGetUserStockHolder().stockDataStream();
    }

    @Override
    public void reset() {
        checkAndGetUserStockHolder().reset();
        queue.enqueue(this);
    }

    public boolean isLoaded() {
        return loadedStockHolderReference.get() != null;
    }

    public void load() {
        synchronized (lock) {
            if (!isLoaded()) {
                loadedStockHolderReference.set(loader.apply(user));
            }
        }
    }

    public void unload() {
        queue.dequeue(this);

        synchronized (lock) {
            if (isLoaded()) {
                loadedStockHolderReference.set(null);
            }
        }
    }

    public @NotNull UserStockHolder getSource() {
        if (isLoaded()) {
            return loadedStockHolderReference.get();
        } else {
            throw new IllegalStateException("The stockholder is not set (" + getUUID() + ")");
        }
    }

    private @NotNull UserStockHolder checkAndGetUserStockHolder() {
        if (!isLoaded()) {
            load();
        }

        return loadedStockHolderReference.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserStockHolderLoader that)) return false;
        return getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser());
    }

    @Override
    public String toString() {
        return "UserStockHolderLoader{" +
                "user=" + user +
                ", isLoaded=" + isLoaded() +
                '}';
    }
}
