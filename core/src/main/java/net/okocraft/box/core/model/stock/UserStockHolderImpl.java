package net.okocraft.box.core.model.stock;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class UserStockHolderImpl extends AbstractStockHolder implements UserStockHolder {

    private final BoxUser user;

    public UserStockHolderImpl(@NotNull BoxUser user) {
        this(user, Collections.emptyList());
    }

    public UserStockHolderImpl(@NotNull BoxUser user, @NotNull Collection<StockData> stockDataCollection) {
        super(stockDataCollection);
        this.user = user;
    }

    @Override
    public @NotNull String getName() {
        return user.getName().orElse("Unknown");
    }

    @Override
    public @NotNull BoxUser getUser() {
        return user;
    }

    @Override
    public boolean isOnline() {
        return Bukkit.getPlayer(user.getUUID()) != null;
    }
}
