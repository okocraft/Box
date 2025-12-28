package net.okocraft.box.feature.stats.provider;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.okocraft.box.feature.stats.database.operator.ItemStatisticsOperator;
import net.okocraft.box.feature.stats.database.operator.StatisticsOperators;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@NotNullByDefault
public class ItemStatisticsProvider {

    private final AtomicReference<@Nullable Int2LongMap> totalAmountByItemId = new AtomicReference<>();
    private final AtomicLong totalAmount = new AtomicLong();
    private final Database database;
    private final ItemStatisticsOperator operator;

    public ItemStatisticsProvider(Database database, StatisticsOperators operators) {
        this.database = database;
        this.operator = operators.itemStatisticsTableOperator();
    }

    public long getTotalAmountByItemId(int itemId) {
        Int2LongMap totalAmountByItemId = this.totalAmountByItemId.get();
        if (totalAmountByItemId == null) {
            return 0;
        }
        return totalAmountByItemId.getOrDefault(itemId, 0);
    }

    public long getTotalAmount() {
        return this.totalAmount.get();
    }

    public float calculateItemPercentage(int itemId) {
        long globalTotalAmount = this.getTotalAmount();
        long itemTotalAmount = this.getTotalAmountByItemId(itemId);
        return globalTotalAmount == 0 ? 0 : (float) itemTotalAmount / (float) globalTotalAmount * 100.0F;
    }

    public void refresh() throws Exception {
        Int2LongMap totalAmountByItemId = new Int2LongOpenHashMap();
        AtomicLong newTotalAmount = new AtomicLong();

        try (Connection connection = this.database.getConnection()) {
            this.operator.selectTotalAmountByItemId(connection, (itemId, amount) -> {
                totalAmountByItemId.put(itemId, amount.intValue());
                newTotalAmount.addAndGet(amount);
            });
        }

        this.totalAmountByItemId.set(totalAmountByItemId);
        this.totalAmount.set(newTotalAmount.get());
    }

    public void clearCache() {
        this.totalAmountByItemId.set(null);
        this.totalAmount.set(0);
    }
}
