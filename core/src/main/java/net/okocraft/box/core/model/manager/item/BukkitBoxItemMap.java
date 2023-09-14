package net.okocraft.box.core.model.manager.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class BukkitBoxItemMap extends BoxItemMap {

    public static @NotNull BukkitBoxItemMap withItems(@NotNull Iterator<BoxItem> initialBoxItemIterator) {
        var itemMap = new BukkitBoxItemMap();
        itemMap.initialize(initialBoxItemIterator);
        return itemMap;
    }

    private final Object2IntMap<ItemStack> itemToId = new Object2IntOpenHashMap<>();

    private BukkitBoxItemMap() {
        super();
        this.itemToId.defaultReturnValue(UNKNOWN_ID);
    }

    public boolean isRegistered(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack);

        {
            long readAttempt = this.lock.tryOptimisticRead();
            boolean result = checkItemAtUnsynchronized(itemStack);

            if (this.lock.validate(readAttempt)) {
                return result;
            }
        }

        this.lock.readLock();

        try {
            return checkItemAtUnsynchronized(itemStack);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public @Nullable BoxItem getByItemStack(@NotNull ItemStack item) {
        Objects.requireNonNull(item);

        if (!item.hasItemMeta()) {
            return getByItemName(item.getType().name());
        }

        var one = item.asOne();

        {
            long readAttempt = this.lock.tryOptimisticRead();
            var boxItem = getByItemStackAtUnsynchronized(one);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return getByItemStackAtUnsynchronized(one);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    public boolean checkItemAtUnsynchronized(@NotNull ItemStack itemStack) {
        return this.itemToId.containsKey(itemStack);
    }

    @Override
    public void addItemAtUnsynchronized(@NotNull BoxItem item) {
        super.addItemAtUnsynchronized(item);
        this.itemToId.put(item.getOriginal(), item.getInternalId());
    }

    @Override
    public void removeItemAtUnsynchronized(@NotNull BoxItem item) {
        super.removeItemAtUnsynchronized(item);
        this.itemToId.removeInt(item.getOriginal());
    }

    private @Nullable BoxItem getByItemStackAtUnsynchronized(@NotNull ItemStack item) {
        return getByIdAtUnsynchronized(this.itemToId.getInt(item));
    }
}
