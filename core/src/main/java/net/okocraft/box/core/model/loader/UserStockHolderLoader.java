package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UserStockHolderLoader implements UserStockHolder {

    private final BoxUser user;
    private final Function<BoxUser, UserStockHolder> loader;
    private final AtomicReference<UserStockHolder> loadedStockHolderReference = new AtomicReference<>();

    public UserStockHolderLoader(@NotNull BoxUser user,
                                 @NotNull Function<BoxUser, UserStockHolder> loader) {
        this.user = user;
        this.loader = loader;
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
    }

    @Override
    public int increase(@NotNull BoxItem item) {
        return checkAndGetUserStockHolder().increase(item);
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment) {
        return checkAndGetUserStockHolder().increase(item, increment);
    }

    @Override
    public int decrease(@NotNull BoxItem item) {
        return checkAndGetUserStockHolder().decrease(item);
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement) {
        return checkAndGetUserStockHolder().decrease(item, decrement);
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
}
