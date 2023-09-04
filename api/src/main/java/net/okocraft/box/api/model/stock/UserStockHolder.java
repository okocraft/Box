package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that holds the user's personal stock.
 * <p>
 * The implementation of this interface must be thread-safe.
 *
 * @deprecated This interface will be removed in Box 6.0.0
 */
@Deprecated(since = "5.5.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public interface UserStockHolder extends StockHolder {

    /**
     * Gets the owner of this holder.
     *
     * @return the owner of this holder
     */
    @NotNull BoxUser getUser();
}
