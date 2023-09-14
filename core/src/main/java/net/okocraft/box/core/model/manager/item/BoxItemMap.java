package net.okocraft.box.core.model.manager.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

public class BoxItemMap {

    public static final int UNKNOWN_ID = Integer.MIN_VALUE;

    private final Object2IntMap<String> itemNameToId = new Object2IntOpenHashMap<>();
    private final Int2ObjectOpenHashMap<BoxItem> idToBoxItem = new Int2ObjectOpenHashMap<>();

    private IntImmutableList copiedItemIdListCache = IntImmutableList.of();
    private ObjectImmutableList<String> copiedItemNameListCache = ObjectImmutableList.of();
    private ObjectImmutableList<BoxItem> copiedBoxItemListCache = ObjectImmutableList.of();

    protected final StampedLock lock = new StampedLock();
    private Thread writeLockOwner;

    public BoxItemMap() {
        this.itemNameToId.defaultReturnValue(UNKNOWN_ID);
    }

    /* Thread-Safe methods */

    public final void acquireWriteLock() {
        final Thread currentThread = Thread.currentThread();
        if (this.writeLockOwner == currentThread) {
            throw new IllegalStateException("Cannot lock twice from the same thread.");
        }
        this.lock.writeLock();
        this.writeLockOwner = currentThread;
    }

    public final void releaseWriteLock() {
        this.writeLockOwner = null;
        this.lock.tryUnlockWrite();
    }

    public boolean isRegistered(@NotNull String itemName) {
        Objects.requireNonNull(itemName);
        {
            long readAttempt = this.lock.tryOptimisticRead();
            boolean result = checkItemNameAtUnsynchronized(itemName);

            if (this.lock.validate(readAttempt)) {
                return result;
            }
        }

        this.lock.readLock();

        try {
            return checkItemNameAtUnsynchronized(itemName);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @Nullable BoxItem getByItemName(@NotNull String itemName) {
        Objects.requireNonNull(itemName);
        {
            long readAttempt = this.lock.tryOptimisticRead();
            var boxItem = getByItemNameAtUnsynchronized(itemName);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return getByItemNameAtUnsynchronized(itemName);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @Nullable BoxItem getById(int id) {
        if (id == UNKNOWN_ID) {
            return null;
        }

        {
            long readAttempt = this.lock.tryOptimisticRead();
            var boxItem = getByIdAtUnsynchronized(id);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return getByIdAtUnsynchronized(id);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @NotNull IntImmutableList getItemIdList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            var list = getItemIdListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return getItemIdListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @NotNull ObjectImmutableList<String> getItemNameList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            var list = getItemNameListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return getItemNameListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @NotNull ObjectImmutableList<BoxItem> getItemList() {
        {
            long readAttempt = this.lock.tryOptimisticRead();
            var list = getBoxItemListAtUnsynchronized();

            if (this.lock.validate(readAttempt)) {
                return list;
            }
        }

        this.lock.readLock();

        try {
            return getBoxItemListAtUnsynchronized();
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    /* Thread-Unsafe methods */

    protected final void initialize(@NotNull Iterator<BoxItem> initialBoxItemIterator) {
        initialBoxItemIterator.forEachRemaining(this::addItemAtUnsynchronized);
        rebuildCache();
    }

    protected final @Nullable BoxItem getByIdAtUnsynchronized(int id) {
        return id != UNKNOWN_ID ? this.idToBoxItem.get(id) : null;
    }

    public boolean checkItemNameAtUnsynchronized(@NotNull String itemName) {
        return this.itemNameToId.containsKey(itemName);
    }

    public void addItemAtUnsynchronized(@NotNull BoxItem item) {
        if (item.getInternalId() == UNKNOWN_ID) {
            throw new IllegalArgumentException("Cannot use " + item.getInternalId() + " as internal id, because this value used for indicating UNKNOWN_ID.");
        }

        this.itemNameToId.put(item.getPlainName(), item.getInternalId());
        this.idToBoxItem.put(item.getInternalId(), item);
    }

    public void removeItemAtUnsynchronized(@NotNull BoxItem item) {
        if (item.getInternalId() == UNKNOWN_ID) {
            return;
        }

        this.itemNameToId.removeInt(item.getPlainName());
        this.idToBoxItem.remove(item.getInternalId());
    }

    public void rebuildCache() {
        this.copiedItemIdListCache = IntImmutableList.of(itemNameToId.values().toIntArray());
        this.copiedItemNameListCache = ObjectImmutableList.of(itemNameToId.keySet().toArray(String[]::new));
        this.copiedBoxItemListCache = ObjectImmutableList.of(idToBoxItem.values().toArray(BoxItem[]::new));
    }

    private @Nullable BoxItem getByItemNameAtUnsynchronized(@NotNull String itemName) {
        return getByIdAtUnsynchronized(this.itemNameToId.getInt(itemName));
    }

    private @NotNull IntImmutableList getItemIdListAtUnsynchronized() {
        return copiedItemIdListCache;
    }

    private @NotNull ObjectImmutableList<String> getItemNameListAtUnsynchronized() {
        return copiedItemNameListCache;
    }

    private @NotNull ObjectImmutableList<BoxItem> getBoxItemListAtUnsynchronized() {
        return copiedBoxItemListCache;
    }
}
