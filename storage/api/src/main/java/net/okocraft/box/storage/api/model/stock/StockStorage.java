package net.okocraft.box.storage.api.model.stock;

import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

public interface StockStorage {

    void init() throws Exception;

    void close() throws Exception;

    @NotNull UserStockHolder loadUserStockHolder(@NotNull BoxUser user) throws Exception;

    void saveUserStockHolder(@NotNull UserStockHolder stockHolder) throws Exception;
}
