package net.okocraft.box.test.shared.model.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.test.shared.model.user.TestUser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;

public final class TestStockHolder {

    public static @NotNull StockHolder create() {
        return create(Collections.emptyList());
    }

    public static @NotNull StockHolder create(@NotNull Collection<StockData> stockData) {
        return create(VoidStockEventCaller.INSTANCE, stockData, id -> null);
    }

    public static @NotNull StockHolder create(@NotNull StockEventCaller eventCaller) {
        return create(eventCaller, id -> null);
    }

    public static @NotNull StockHolder create(@NotNull StockEventCaller eventCaller, @NotNull IntFunction<BoxItem> toBoxItem) {
        return create(eventCaller, Collections.emptyList(), toBoxItem);
    }

    private static @NotNull StockHolder create(@NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData, @NotNull IntFunction<BoxItem> toBoxItem) {
        return StockHolderFactory.create(TestUser.USER, eventCaller, stockData, toBoxItem);
    }

    private TestStockHolder() {
        throw new UnsupportedOperationException();
    }
}
