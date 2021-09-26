package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that holds the user's personal stock.
 * <p>
 * The implementation of this interface must be thread-safe.
 */
public interface UserStockHolder extends StockHolder {

    /**
     * Gets the owner of this holder.
     *
     * @return the owner of this holder
     */
    @NotNull BoxUser getUser();

    /**
     * Checks if the owner of this holder is online.
     *
     * @return whether the owner of this holder is online or not
     */
    boolean isOnline();
}
