package net.okocraft.box.feature.autostore.setting;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.StampedLock;

/**
 * A class to hold the user's per-item settings.
 */
public class PerItemSetting {

    private final IntSet enabledItemIds = new IntOpenHashSet();
    private final StampedLock lock = new StampedLock();

    /**
     * Checks if the {@link BoxItem} is enabled.
     *
     * @param item the {@link BoxItem} to check
     * @return if {@code true}, the {@link BoxItem} is enabled in this setting, or if {@code false}, it is disabled
     */
    public boolean isEnabled(@NotNull BoxItem item) {
        {
            long stamp = this.lock.tryOptimisticRead();
            boolean result = this.enabledItemIds.contains(item.getInternalId());

            if (this.lock.validate(stamp)) {
                return result;
            }
        }

        long stamp = this.lock.readLock();

        try {
            return this.enabledItemIds.contains(item.getInternalId());
        } finally {
            this.lock.unlockRead(stamp);
        }
    }

    /**
     * Enables or disables the auto-store of the specified {@link BoxItem}.
     *
     * @param item    the {@link BoxItem} to change the setting
     * @param enabled {@code true} to enable, or {@code false} to disable
     */
    public void setEnabled(@NotNull BoxItem item, boolean enabled) {
        long stamp = this.lock.writeLock();

        try {
            if (enabled) {
                this.enabledItemIds.add(item.getInternalId());
            } else {
                this.enabledItemIds.remove(item.getInternalId());
            }
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /**
     * Toggles the auto-store of the specified {@link BoxItem}.
     *
     * @param item the {@link BoxItem} to toggle
     * @return the current setting of the {@link BoxItem}
     */
    public boolean toggleEnabled(@NotNull BoxItem item) {
        long stamp = this.lock.writeLock();
        boolean result;

        try {
            if (this.enabledItemIds.contains(item.getInternalId())) {
                this.enabledItemIds.remove(item.getInternalId());
                result = false;
            } else {
                this.enabledItemIds.add(item.getInternalId());
                result = true;
            }
        } finally {
            this.lock.unlockWrite(stamp);
        }

        return result;
    }

    /**
     * Gets all enabled item ids.
     *
     * @return all enabled item ids
     */
    public @NotNull IntSet getEnabledItems() {
        long stamp = this.lock.readLock();

        try {
            return IntSets.unmodifiable(new IntOpenHashSet(this.enabledItemIds));
        } finally {
            this.lock.unlockRead(stamp);
        }
    }

    /**
     * Clears and enables specified items.
     */
    public void clearAndEnableItems(@NotNull IntCollection itemIds) {
        long stamp = this.lock.writeLock();

        try {
            this.enabledItemIds.clear();
            this.enabledItemIds.addAll(itemIds);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public String toString() {
        return "PerItemModeSetting{" +
                "enabledItems=" + this.enabledItemIds +
                '}';
    }
}
