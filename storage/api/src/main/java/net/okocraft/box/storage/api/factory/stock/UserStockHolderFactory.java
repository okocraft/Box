package net.okocraft.box.storage.api.factory.stock;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public final class UserStockHolderFactory {

    @Contract("_ -> new")
    public static @NotNull UserStockHolder create(@NotNull BoxUser user) {
        return create(user, Collections.emptyList());
    }

    @Contract("_, _ -> new")
    public static @NotNull UserStockHolder create(@NotNull BoxUser user, @NotNull Collection<StockData> stockData) {
        return new UserStockHolderImpl(user, stockData);
    }

    private UserStockHolderFactory() {
        throw new UnsupportedOperationException();
    }
}
