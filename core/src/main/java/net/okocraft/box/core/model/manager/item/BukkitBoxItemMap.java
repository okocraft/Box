package net.okocraft.box.core.model.manager.item;

import dev.siroshun.event4j.api.caller.EventCaller;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.item.ItemImportEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

class BukkitBoxItemMap extends BoxItemMap {

    static @NotNull BukkitBoxItemMap withItems(@NotNull Iterator<BoxItem> initialBoxItemIterator, @NotNull EventCaller<BoxEvent> eventCaller) {
        var itemMap = new BukkitBoxItemMap();

        initialBoxItemIterator.forEachRemaining(item -> {
            itemMap.addItemAtUnsynchronized(item);
            eventCaller.call(new ItemImportEvent(item));
        });

        itemMap.rebuildCache();

        return itemMap;
    }

    private final Object2IntMap<ItemStack> itemToId = new Object2IntOpenHashMap<>();

    private BukkitBoxItemMap() {
        super();
        this.itemToId.defaultReturnValue(UNKNOWN_ID);
    }

    boolean isRegistered(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack);

        {
            long readAttempt = this.lock.tryOptimisticRead();
            boolean result = this.checkItemAtUnsynchronized(itemStack);

            if (this.lock.validate(readAttempt)) {
                return result;
            }
        }

        this.lock.readLock();

        try {
            return this.checkItemAtUnsynchronized(itemStack);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    @Nullable BoxItem getByItemStack(@NotNull ItemStack item) {
        Objects.requireNonNull(item);

        if (!item.hasItemMeta() && item.getType() != Material.FIREWORK_ROCKET) {
            return this.getByItemName(item.getType().name());
        }

        var one = item.getAmount() == 1 ? item : item.asOne();

        {
            long readAttempt = this.lock.tryOptimisticRead();
            var boxItem = this.getByItemStackAtUnsynchronized(one);

            if (this.lock.validate(readAttempt)) {
                return boxItem;
            }
        }

        this.lock.readLock();

        try {
            return this.getByItemStackAtUnsynchronized(one);
        } finally {
            this.lock.tryUnlockRead();
        }
    }

    boolean checkItemAtUnsynchronized(@NotNull ItemStack itemStack) {
        return this.itemToId.containsKey(itemStack);
    }

    @Override
    void addItemAtUnsynchronized(@NotNull BoxItem item) {
        super.addItemAtUnsynchronized(item);
        this.itemToId.put(item.getOriginal(), item.getInternalId());
    }

    @Override
    void removeItemAtUnsynchronized(@NotNull BoxItem item) {
        super.removeItemAtUnsynchronized(item);
        this.itemToId.removeInt(item.getOriginal());
    }

    private @Nullable BoxItem getByItemStackAtUnsynchronized(@NotNull ItemStack item) {
        return this.getByIdAtUnsynchronized(this.itemToId.getInt(item));
    }
}
