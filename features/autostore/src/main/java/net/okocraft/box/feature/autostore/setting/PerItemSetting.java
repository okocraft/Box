package net.okocraft.box.feature.autostore.setting;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A class to hold the user's per-item settings.
 */
public class PerItemSetting {

    private final IntSet enabledItemIds = new IntOpenHashSet();
    private final Object lock = new Object();
    private volatile IntSet snapshot = IntSet.of();

    /**
     * Checks if the {@link BoxItem} is enabled.
     *
     * @param item the {@link BoxItem} to check
     * @return if {@code true}, the {@link BoxItem} is enabled in this setting, or if {@code false}, it is disabled
     */
    public boolean isEnabled(@NotNull BoxItem item) {
        return this.snapshot.contains(item.getInternalId());
    }

    /**
     * Enables or disables the auto-store of the specified {@link BoxItem}.
     *
     * @param item    the {@link BoxItem} to change the setting
     * @param enabled {@code true} to enable, or {@code false} to disable
     */
    public void setEnabled(@NotNull BoxItem item, boolean enabled) {
        synchronized (this.lock) {
            if (enabled) {
                this.enabledItemIds.add(item.getInternalId());
            } else {
                this.enabledItemIds.remove(item.getInternalId());
            }
            this.snapshot = IntSet.of(this.enabledItemIds.toIntArray());
        }
    }

    /**
     * Toggles the auto-store of the specified {@link BoxItem}.
     *
     * @param item the {@link BoxItem} to toggle
     * @return the current setting of the {@link BoxItem}
     */
    public boolean toggleEnabled(@NotNull BoxItem item) {
        boolean result;
        synchronized (this.lock) {
            if (this.enabledItemIds.contains(item.getInternalId())) {
                this.enabledItemIds.remove(item.getInternalId());
                result = false;
            } else {
                this.enabledItemIds.add(item.getInternalId());
                result = true;
            }
            this.snapshot = IntSet.of(this.enabledItemIds.toIntArray());
        }
        return result;
    }

    /**
     * Gets all enabled item ids.
     *
     * @return all enabled item ids
     */
    public @NotNull IntSet getEnabledItems() {
        return this.snapshot;
    }

    /**
     * Clears and enables items.
     */
    public void clearAndEnableItems(@NotNull IntCollection itemIds) {
        synchronized (this.lock) {
            this.enabledItemIds.clear();
            if (itemIds.isEmpty()) {
                this.snapshot = IntSet.of();
            } else {
                this.enabledItemIds.addAll(itemIds);
                this.snapshot = IntSet.of(this.enabledItemIds.toIntArray());
            }
        }
    }

    @Override
    public String toString() {
        return "PerItemModeSetting{" +
                "enabledItems=" + this.enabledItemIds +
                '}';
    }
}
