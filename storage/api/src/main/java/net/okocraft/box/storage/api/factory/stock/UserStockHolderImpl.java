package net.okocraft.box.storage.api.factory.stock;

import net.okocraft.box.api.model.stock.AbstractStockHolder;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

class UserStockHolderImpl extends AbstractStockHolder implements UserStockHolder {

    private final BoxUser user;

    UserStockHolderImpl(@NotNull BoxUser user, @NotNull Collection<StockData> stockDataCollection) {
        super(stockDataCollection);
        this.user = Objects.requireNonNull(user);
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
}
