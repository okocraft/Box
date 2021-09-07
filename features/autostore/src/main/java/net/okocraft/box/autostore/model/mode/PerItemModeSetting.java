package net.okocraft.box.autostore.model.mode;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PerItemModeSetting implements AutoStoreMode {

    private final Set<BoxItem> enabledItems = new HashSet<>();

    @Override
    public @NotNull String getModeName() {
        return "per-item";
    }

    @Override
    public boolean isEnabled(@NotNull BoxItem item) {
        return enabledItems.contains(item);
    }

    public void setEnabled(@NotNull BoxItem item, boolean enabled) {
        if (enabled) {
            enabledItems.add(item);
        } else {
            enabledItems.remove(item);
        }
    }

    public boolean toggleEnabled(@NotNull BoxItem item) {
        var toggled = !isEnabled(item);

        setEnabled(item, toggled);

        return toggled;
    }

    public @NotNull Set<BoxItem> getEnabledItems() {
        return enabledItems;
    }

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
