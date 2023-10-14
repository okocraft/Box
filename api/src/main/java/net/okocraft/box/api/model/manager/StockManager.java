package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * An interface to manage {@link StockHolder}s.
 */
public interface StockManager {

    /**
     * Gets the {@link PersonalStockHolder} of the specified {@link BoxUser}.
     *
     * @param user the {@link BoxUser} to get {@link PersonalStockHolder}
     * @return the {@link PersonalStockHolder} of the specified {@link BoxUser}
     */
    @NotNull PersonalStockHolder getPersonalStockHolder(@NotNull BoxUser user);

    /**
     * Creates an implementation of {@link StockHolder}.
     *
     * @param uuid        the {@link UUID} of the {@link StockHolder}
     * @param name        the name of the {@link StockHolder}
     * @param eventCaller the {@link StockEventCaller} to call {@link net.okocraft.box.api.event.stockholder.stock.StockEvent}s
     * @return a new {@link StockHolder} instance
     */
    @Contract("_, _, _ -> new")
    @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller);

    /**
     * Creates an implementation of {@link StockHolder} with a collection of {@link StockData}.
     *
     * @param uuid        the {@link UUID} of the {@link StockHolder}
     * @param name        the name of the {@link StockHolder}
     * @param eventCaller the {@link StockEventCaller} to call {@link net.okocraft.box.api.event.stockholder.stock.StockEvent}s
     * @param stockData   a collection of {@link StockData}
     * @return a new {@link StockHolder} instance
     */
    @Contract("_, _, _, _ -> new")
    @NotNull StockHolder createStockHolder(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData);
}
