package net.okocraft.box.core.model.loader;

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

public class UserStockHolderLoader implements UserStockHolder {

    private final BoxUser user;
    private final Function<BoxUser, UserStockHolder> loader;
    private final AutoSaveQueue queue;

    private final AtomicReference<UserStockHolder> loadedStockHolderReference = new AtomicReference<>();

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
    public void setAmount(@NotNull BoxItem item, int amount) {
        checkAndGetUserStockHolder().setAmount(item, amount);
        queue.enqueue(this);
    }

    @Override
    public int increase(@NotNull BoxItem item) {
        int current = checkAndGetUserStockHolder().increase(item);
        queue.enqueue(this);
        return current;
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment) {
        int current = checkAndGetUserStockHolder().increase(item, increment);
        queue.enqueue(this);
        return current;
    }

    @Override
    public int decrease(@NotNull BoxItem item) {
        int current = checkAndGetUserStockHolder().decrease(item);
        queue.enqueue(this);
        return current;
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement) {
        int current = checkAndGetUserStockHolder().decrease(item, decrement);
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

    public boolean isLoaded() {
        return loadedStockHolderReference.get() != null;
    }

    public void load() {
        loadedStockHolderReference.set(loader.apply(user));
    }

    public void unload() {
        queue.dequeue(this);
        loadedStockHolderReference.set(null);
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
