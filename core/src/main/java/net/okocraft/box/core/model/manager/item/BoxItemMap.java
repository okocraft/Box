package net.okocraft.box.core.model.manager.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

class BoxItemMap {

    static final int UNKNOWN_ID = Integer.MIN_VALUE;

    private final Object2IntMap<String> itemNameToId = new Object2IntOpenHashMap<>();
    private final Int2ObjectOpenHashMap<BoxItem> idToBoxItem = new Int2ObjectOpenHashMap<>();

    private IntImmutableList copiedItemIdListCache = IntImmutableList.of();
    private ObjectImmutableList<String> copiedItemNameListCache = ObjectImmutableList.of();
    private ObjectImmutableList<BoxItem> copiedBoxItemListCache = ObjectImmutableList.of();

    protected final StampedLock lock = new StampedLock();
    private Thread writeLockOwner;

    BoxItemMap() {
        this.itemNameToId.defaultReturnValue(UNKNOWN_ID);
    }

    /* Thread-Safe methods */

    final void acquireWriteLock() {
        final Thread currentThread = Thread.currentThread();
        if (this.writeLockOwner == currentThread) {
            throw new IllegalStateException("Cannot lock twice from the same thread.");
        }
        this.lock.writeLock();
        this.writeLockOwner = currentThread;
    }

    final void releaseWriteLock() {
        this.writeLockOwner = null;
        this.lock.tryUnlockWrite();
    }

    boolean isRegistered(@NotNull String itemName) {
        Objects.requireNonNull(itemName);
        {
            long readAttempt = this.lock.tryOptimisticRead();
            boolean result = this.checkItemNameAtUnsynchronized(itemName);

            if (this.lock.validate(readAttempt)) {
                return result;
            }
        }

        this.lock.readLock();

        try {
            return this.checkItemNameAtUnsynchronized(itemName);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @Nullable BoxItem getByItemName(@NotNull String itemName) {
        Objects.requireNonNull(itemName);
        {
            long readAttempt = this.lock.tryOptimisticRead();
            BoxItem boxItem = this.getByItemNameAtUnsynchronized(itemName);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return this.getByItemNameAtUnsynchronized(itemName);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @Nullable BoxItem getById(int id) {
        if (id == UNKNOWN_ID) {
            return null;
        }

        {
            long readAttempt = this.lock.tryOptimisticRead();
            BoxItem boxItem = this.getByIdAtUnsynchronized(id);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return this.getByIdAtUnsynchronized(id);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @NotNull IntImmutableList getItemIdList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            IntImmutableList list = this.getItemIdListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return this.getItemIdListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @NotNull ObjectImmutableList<String> getItemNameList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            ObjectImmutableList<String> list = this.getItemNameListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return this.getItemNameListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @NotNull ObjectImmutableList<BoxItem> getItemList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            ObjectImmutableList<BoxItem> list = this.getBoxItemListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return this.getBoxItemListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    /* Thread-Unsafe methods */

    protected final @Nullable BoxItem getByIdAtUnsynchronized(int id) {
        return id != UNKNOWN_ID ? this.idToBoxItem.get(id) : null;
    }

    boolean checkItemNameAtUnsynchronized(@NotNull String itemName) {
        return this.itemNameToId.containsKey(itemName);
    }

    void addItemAtUnsynchronized(@NotNull BoxItem item) {
        if (item.getInternalId() == UNKNOWN_ID) {
            throw new IllegalArgumentException("Cannot use " + item.getInternalId() + " as internal id, because this value used for indicating UNKNOWN_ID.");
        }

        this.itemNameToId.put(item.getPlainName(), item.getInternalId());
        this.idToBoxItem.put(item.getInternalId(), item);
    }

    void removeItemAtUnsynchronized(@NotNull BoxItem item) {
        if (item.getInternalId() == UNKNOWN_ID) {
            return;
        }

        this.itemNameToId.removeInt(item.getPlainName());
        this.idToBoxItem.remove(item.getInternalId());
    }

    void rebuildCache() {
        this.copiedItemIdListCache = IntImmutableList.of(this.itemNameToId.values().toIntArray());
        this.copiedItemNameListCache = ObjectImmutableList.of(this.itemNameToId.keySet().toArray(String[]::new));
        this.copiedBoxItemListCache = ObjectImmutableList.of(this.idToBoxItem.values().toArray(BoxItem[]::new));
    }

    private @Nullable BoxItem getByItemNameAtUnsynchronized(@NotNull String itemName) {
        return this.getByIdAtUnsynchronized(this.itemNameToId.getInt(itemName));
    }

    private @NotNull IntImmutableList getItemIdListAtUnsynchronized() {
        return this.copiedItemIdListCache;
    }

    private @NotNull ObjectImmutableList<String> getItemNameListAtUnsynchronized() {
        return this.copiedItemNameListCache;
    }

    private @NotNull ObjectImmutableList<BoxItem> getBoxItemListAtUnsynchronized() {
        return this.copiedBoxItemListCache;
    }
}
