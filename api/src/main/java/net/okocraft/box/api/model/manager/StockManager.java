package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An interface to manage {@link net.okocraft.box.api.model.stock.StockHolder}s.
 */
public interface StockManager {

    /**
     * Loads an {@link UserStockHolder}.
     *
     * @param user the {@link BoxUser}
     * @return the {@link CompletableFuture} to load user's {@link net.okocraft.box.api.model.stock.StockHolder}.
     * @deprecated {@link UserStockHolder} will be removed in Box 6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull CompletableFuture<@NotNull UserStockHolder> loadUserStock(@NotNull BoxUser user);

    /**
     * Saves an {@link UserStockHolder}.
     *
     * @param userStockHolder {@link UserStockHolder} to save
     * @return the {@link CompletableFuture} to save user's {@link net.okocraft.box.api.model.stock.StockHolder}.
     * @deprecated {@link UserStockHolder} will be removed in Box 6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull CompletableFuture<Void> saveUserStock(@NotNull UserStockHolder userStockHolder);

    /**
     * Gets the {@link BoxUser}'s {@link StockHolder}.
     *
     * @param user the {@link BoxUser} to get {@link StockHolder}
     * @return the {@link BoxUser}'s {@link StockHolder}
     */
    @NotNull StockHolder getPersonalStockHolderLoader(@NotNull BoxUser user);
}
