package net.okocraft.box.feature.autostore.model.setting;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to hold the user's per-item settings.
 */
public class PerItemSetting {

    private final Set<BoxItem> enabledItems = Collections.synchronizedSet(new HashSet<>());

    /**
     * Checks if the {@link BoxItem} is included in {@link #getEnabledItems()}.
     *
     * @param item the {@link BoxItem} to check
     * @return if {@code true}, the {@link BoxItem} is enabled in this setting, or if {@code false}, it is disabled
     */
    public boolean isEnabled(@NotNull BoxItem item) {
        return enabledItems.contains(item);
    }

    /**
     * Enables or disables the auto-store of the specified {@link BoxItem}.
     *
     * @param item    the {@link BoxItem} to change the setting
     * @param enabled {@code true} to enable, or {@code false} to disable
     */
    public void setEnabled(@NotNull BoxItem item, boolean enabled) {
        if (enabled) {
            enabledItems.add(item);
        } else {
            enabledItems.remove(item);
        }
    }

    /**
     * Toggles the auto-store of the specified {@link BoxItem}.
     *
     * @param item the {@link BoxItem} to toggle
     * @return the current setting of the {@link BoxItem}
     */
    public boolean toggleEnabled(@NotNull BoxItem item) {
        var toggled = !isEnabled(item);

        setEnabled(item, toggled);

        return toggled;
    }

    /**
     * Gets the set of all enabled items
     *
     * @return the set of all enabled items
     */
    public @NotNull @UnmodifiableView Set<BoxItem> getEnabledItems() {
        return Collections.unmodifiableSet(enabledItems);
    }

    /**
     * Sets the set of enabled items.
     * <p>
     * This method cleans current enabled items, and set the given items.
     *
     * @param items the set of enabled items
     */
    public void setEnabledItems(@NotNull Collection<BoxItem> items) {
        enabledItems.clear();
        enabledItems.addAll(items);
    }

    @Override
    public String toString() {
        return "PerItemModeSetting{" +
                "enabledItems=" + enabledItems +
                '}';
    }
}
