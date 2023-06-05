package net.okocraft.box.feature.category.api.category;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

class CategoryImpl implements Category {

    private final Component displayName;
    private final Material iconMaterial;
    private final boolean shouldSave;
    private final List<BoxItem> items = new ArrayList<>();
    private final Object lock = new Object();

    private volatile List<BoxItem> snapshot = null;
    private volatile IntSet itemIds;

    CategoryImpl(@NotNull Component displayName, @NotNull Material iconMaterial, boolean shouldSave) {
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.shouldSave = shouldSave;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return iconMaterial;
    }

    @Override
    public @NotNull @Unmodifiable List<BoxItem> getItems() {
        if (snapshot == null) {
            synchronized (lock) {
                snapshot = List.copyOf(items);
            }
        }

        return snapshot;
    }

    @Override
    public void addItem(@NotNull BoxItem item) {
        synchronized (lock) {
            items.add(item);
            updateSnapshot();
        }
    }

    @Override
    public void removeItem(@NotNull BoxItem item) {
        synchronized (lock) {
            items.remove(item);
            updateSnapshot();
        }
    }

    @Override
    public boolean containsItem(@NotNull BoxItem item) {
        if (itemIds == null) {
            synchronized (lock) {
                itemIds = collectItemIds(new IntOpenHashSet());
            }
        }
        return itemIds.contains(item.getInternalId());
    }

    @Override
    public boolean shouldSave() {
        return shouldSave;
    }

    private void updateSnapshot() {
        // if the items are already copied, update them.
        if (snapshot != null) {
            snapshot = List.copyOf(items);
        }

        if (itemIds != null) {
            itemIds.clear();
            collectItemIds(itemIds);
        }
    }

    private @NotNull IntSet collectItemIds(@NotNull IntSet set) {
        for (var item : items) {
            set.add(item.getInternalId());
        }
        return set;
    }
}
