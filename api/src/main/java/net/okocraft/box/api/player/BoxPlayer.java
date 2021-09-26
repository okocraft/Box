package net.okocraft.box.api.player;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An interface of the online {@link BoxUser}.
 */
public interface BoxPlayer extends BoxUser {

    /**
     * Gets the {@link Player} instance.
     *
     * @return the {@link Player}
     */
    @NotNull Player getPlayer();

    /**
     * Gets the {@link UserStockHolder} of this user.
     *
     * @return the {@link UserStockHolder} of this user
     */
    @NotNull UserStockHolder getUserStockHolder();

    /**
     * Gets the current {@link StockHolder} to deposit or withdraw.
     *
     * @return the current {@link StockHolder}
     */
    @NotNull StockHolder getCurrentStockHolder();

    /**
     * Sets the {@link StockHolder} to deposit or withdraw.
     *
     * @param stockHolder the {@link StockHolder}
     */
    void setCurrentStockHolder(@NotNull StockHolder stockHolder);
}
