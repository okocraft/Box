package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that wraps the {@link StockHolder} owned by a user
 */
public interface PersonalStockHolder extends StockHolderWrapper {

    /**
     * Gets the {@link BoxUser} who has this {@link StockHolder}.
     *
     * @return the {@link BoxUser} who has this {@link StockHolder}
     */
    @NotNull BoxUser getUser();

}
