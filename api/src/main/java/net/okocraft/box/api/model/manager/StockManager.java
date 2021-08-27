package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
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
     */
    @NotNull CompletableFuture<@NotNull UserStockHolder> loadUserStock(@NotNull BoxUser user);

    /**
     * Saves an {@link UserStockHolder}.
     *
     * @param userStockHolder {@link UserStockHolder} to save
     * @return the {@link CompletableFuture} to save user's {@link net.okocraft.box.api.model.stock.StockHolder}.
     */
    @NotNull CompletableFuture<Void> saveUserStock(@NotNull UserStockHolder userStockHolder);
}
