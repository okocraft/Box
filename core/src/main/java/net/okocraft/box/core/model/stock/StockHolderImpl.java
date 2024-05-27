package net.okocraft.box.core.model.stock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;

class StockHolderImpl implements StockHolder {

    private final UUID uuid;
    private final NameHolder nameHolder;
    private final StockEventCaller eventCaller;
    private final Int2ObjectOpenHashMap<Stock> stockMap;
    private final IntFunction<BoxItem> toBoxItem;
    private final StampedLock lock = new StampedLock();

    StockHolderImpl(@NotNull UUID uuid, @NotNull NameHolder nameHolder, @NotNull StockEventCaller eventCaller, @NotNull Int2ObjectOpenHashMap<Stock> stockMap,
                    @NotNull IntFunction<BoxItem> toBoxItem) {
        this.uuid = uuid;
        this.nameHolder = nameHolder;
        this.eventCaller = eventCaller;
        this.stockMap = stockMap;
        this.toBoxItem = toBoxItem;
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    @Override
    public @NotNull String getName() {
        return this.nameHolder.get();
    }

    @Override
    public int getAmount(int itemId) {
        var stock = this.getStockOrNull(itemId);
        return stock != null ? stock.get() : 0;
    }

    @Override
    public void setAmount(@NotNull BoxItem item, int amount, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(item);

        if (amount < 0) {
            throw new IllegalArgumentException("amount must be zero or positive");
        }

        Objects.requireNonNull(cause);

        int internalId = item.getInternalId();
        var stock = this.getStockOrNull(internalId);

        if (amount == 0 && stock == null) {
            return;
        }

        if (stock == null) {
            stock = this.getStockOrPutNewStock(internalId, amount);

            if (stock == null) {
                this.eventCaller.callSetEvent(this, item, amount, 0, cause);
                return;
            }
        }

        int previousAmount = stock.set(amount);

        if (previousAmount != amount) {
            this.eventCaller.callSetEvent(this, item, amount, previousAmount, cause);
        }
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(item);

        if (increment < 0) {
            throw new IllegalArgumentException("increment must be zero or positive");
        }

        Objects.requireNonNull(cause);

        if (increment == 0) {
            return this.getAmount(item);
        }

        int internalId = item.getInternalId();
        var stock = this.getStockOrNull(internalId);

        if (stock == null) {
            stock = this.getStockOrPutNewStock(internalId, increment);

            if (stock == null) {
                this.eventCaller.callIncreaseEvent(this, item, increment, increment, cause);
                return increment;
            }
        }

        var result = stock.add(increment);

        if (result.getClass() == Stock.ModifyResult.Success.class) {
            this.eventCaller.callIncreaseEvent(this, item, increment, result.newValue(), cause);
            return result.newValue();
        } else {
            int excess = ((Stock.ModifyResult.Overflow) result).excess();
            this.eventCaller.callOverflowEvent(this, item, increment - excess, excess, cause);
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
        return this.decrease(item, decrement, cause, RETURN_NEW_AMOUNT);
    }

    @Override
    public int decreaseToZero(@NotNull BoxItem item, int limit, @NotNull StockEvent.Cause cause) {
        return this.decrease(item, limit, cause, RETURN_DECREMENT);
    }

    private static final int RETURN_NEW_AMOUNT = 0;
    private static final int RETURN_DECREMENT = 1;

    private int decrease(@NotNull BoxItem item, int limit, @NotNull StockEvent.Cause cause, int returnType) {
        Objects.requireNonNull(item);

        if (limit < 0) {
            throw new IllegalArgumentException("decrement must be zero or positive");
        }

        Objects.requireNonNull(cause);

        if (limit == 0) {
            return returnType == RETURN_NEW_AMOUNT ? this.getAmount(item) : 0;
        }

        var stock = this.getStockOrNull(item.getInternalId());

        if (stock == null) {
            return 0;
        }

        var result = stock.subtract(limit);

        if (result.oldValue() != 0) {
            int decrement = result.oldValue() - result.newValue();

            this.eventCaller.callDecreaseEvent(this, item, decrement, result.newValue(), cause);

            if (returnType == RETURN_NEW_AMOUNT) {
                return result.newValue();
            } else if (returnType == RETURN_DECREMENT) {
                return decrement;
            }
        }

        return 0;
    }

    @Override
    public int decreaseIfPossible(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(item);

        if (decrement < 0) {
            throw new IllegalArgumentException("decrement must be zero or positive");
        }

        Objects.requireNonNull(cause);

        if (decrement == 0) {
            return this.getAmount(item);
        }

        var stock = this.getStockOrNull(item.getInternalId());

        if (stock == null) {
            return -1;
        }

        var result = stock.trySubtract(decrement);

        if (result == null) {
            return -1;
        }

        this.eventCaller.callDecreaseEvent(this, item, decrement, result.newValue(), cause);
        return result.newValue();
    }

    @Override
    public boolean decreaseIfPossible(@NotNull Object2IntMap<BoxItem> decrementMap, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(cause);

        if (decrementMap.isEmpty()) {
            return true;
        }

        long stamp = this.lock.writeLock();
        Object2IntArrayMap<BoxItem> newAmountMap;

        try {
            newAmountMap = this.decreaseIfPossibleAtUnsynchronized(decrementMap);
        } finally {
            this.lock.unlockWrite(stamp);
        }

        if (newAmountMap == null) {
            return false;
        }

        for (var entry : newAmountMap.object2IntEntrySet()) {
            this.eventCaller.callDecreaseEvent(this, entry.getKey(), decrementMap.getInt(entry.getKey()), entry.getIntValue(), cause);
        }

        return true;
    }

    private @Nullable Object2IntArrayMap<BoxItem> decreaseIfPossibleAtUnsynchronized(@NotNull Object2IntMap<BoxItem> decrementMap) {
        Object2IntArrayMap<BoxItem> newAmountMap = new Object2IntArrayMap<>(decrementMap.size());

        for (var entry : decrementMap.object2IntEntrySet()) {
            int decrement = entry.getIntValue();

            if (decrement < 0) {
                throw new IllegalArgumentException("the value in the decrementMap must be zero or positive.");
            } else if (decrement == 0) {
                continue;
            }

            var stock = this.getStockAtUnsynchronized(entry.getKey().getInternalId());

            if (stock == null) {
                return null;
            }

            int newAmount = stock.get() - decrement;

            if (0 <= newAmount) {
                newAmountMap.put(entry.getKey(), newAmount);
            } else {
                return null;
            }
        }

        for (var entry : newAmountMap.object2IntEntrySet()) {
            int internalId = entry.getKey().getInternalId();
            var stock = this.getStockAtUnsynchronized(internalId);

            if (stock == null) {
                this.putNewStockAtUnsynchronized(internalId, entry.getIntValue());
            } else {
                stock.set(entry.getIntValue());
            }
        }

        return newAmountMap;
    }

    @Override
    public @NotNull @Unmodifiable Collection<StockData> reset() {
        Collection<StockData> stockDataCollection;

        long stamp = this.lock.writeLock();

        try {
            stockDataCollection = this.createStockDataAtUnsynchronized();
            this.stockMap.clear();
        } finally {
            this.lock.unlockWrite(stamp);
        }

        this.eventCaller.callResetEvent(this, stockDataCollection);

        return stockDataCollection;
    }

    @Override
    public @NotNull Collection<StockData> toStockDataCollection() {
        Collection<StockData> stockDataCollection;

        long stamp = this.lock.readLock();

        try {
            stockDataCollection = this.createStockDataAtUnsynchronized();
        } finally {
            this.lock.unlockRead(stamp);
        }

        return stockDataCollection;
    }

    @Override
    public String toString() {
        var builder =
                new StringBuilder(getClass().getSimpleName())
                        .append("{name=").append(getName())
                        .append(", uuid=").append(this.uuid)
                        .append(", stockMap={");
        this.writeStockMap(builder);
        return builder.append("}}").toString();
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
        ObjectList<BoxItem> stockDataCollection;

        long stamp = this.lock.readLock();

        try {
            ObjectSet<Int2ObjectMap.Entry<Stock>> entrySet = this.stockMap.int2ObjectEntrySet();
            stockDataCollection = new ObjectArrayList<>(entrySet.size());

            for (var entry : entrySet) {
                if (entry.getValue().get() <= 0) {
                    continue;
                }

                var item = this.toBoxItem.apply(entry.getIntKey());

                if (item != null) {
                    stockDataCollection.add(item);
                }
            }
        } finally {
            this.lock.unlockRead(stamp);
        }

        return ObjectLists.unmodifiable(stockDataCollection);
    }

    private void writeStockMap(@NotNull StringBuilder builder) {
        long stamp = this.lock.readLock();

        try {
            var iterator = this.stockMap.int2ObjectEntrySet().fastIterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                builder.append(entry.getIntKey()).append('=').append(entry.getValue().get());

                if (iterator.hasNext()) {
                    builder.append(',').append(' ');
                }
            }
        } finally {
            this.lock.unlockRead(stamp);
        }
    }

    private @Nullable Stock getStockOrNull(int internalId) {
        Stock stock;

        {
            long readAttempt = this.lock.tryOptimisticRead();
            stock = this.getStockAtUnsynchronized(internalId);

            if (!this.lock.validate(readAttempt)) {
                long stamp = this.lock.readLock();

                try {
                    stock = this.getStockAtUnsynchronized(internalId);
                } finally {
                    this.lock.unlockRead(stamp);
                }
            }
        }

        return stock;
    }

    private @Nullable Stock getStockOrPutNewStock(int internalId, int initialValue) {
        long stamp = this.lock.writeLock();

        try {
            var stock = this.stockMap.get(internalId);

            if (stock == null) {
                this.putNewStockAtUnsynchronized(internalId, initialValue);
                return null;
            } else {
                return stock;
            }
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    private @Nullable Stock getStockAtUnsynchronized(int internalId) {
        return this.stockMap.get(internalId);
    }

    private void putNewStockAtUnsynchronized(int internalId, int initialValue) {
        this.stockMap.put(internalId, new Stock(initialValue));
    }

    private @NotNull @Unmodifiable Collection<StockData> createStockDataAtUnsynchronized() {
        var entrySet = this.stockMap.int2ObjectEntrySet();
        var stockDataCollection = new ObjectArrayList<StockData>(entrySet.size());

        for (var entry : entrySet) {
            int amount = entry.getValue().get();
            if (0 < amount) {
                stockDataCollection.add(new StockData(entry.getIntKey(), amount));
            }
        }

        return Collections.unmodifiableList(stockDataCollection);
    }
}
