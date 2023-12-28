package net.okocraft.box.feature.category.internal.category;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

abstract class AbstractCategory implements Category {

    private final List<BoxItem> items = new ArrayList<>();
    private final Object lock = new Object();

    private volatile List<BoxItem> snapshot = Collections.emptyList();
    private volatile IntSet itemIds = IntSet.of();

    @Override
    public @NotNull @Unmodifiable List<BoxItem> getItems() {
        return this.snapshot;
    }

    @Override
    public void addItem(@NotNull BoxItem item) {
        synchronized (this.lock) {
            this.items.add(item);
            this.recreateSnapshots();
        }
    }

    @Override
    public void addItems(@NotNull Collection<BoxItem> items) {
        synchronized (this.lock) {
            this.items.addAll(items);
            this.recreateSnapshots();
        }
    }

    @Override
    public void removeItem(@NotNull BoxItem item) {
        synchronized (this.lock) {
            this.items.remove(item);
            this.recreateSnapshots();
        }
    }

    @Override
    public boolean containsItem(@NotNull BoxItem item) {
        return this.itemIds.contains(item.getInternalId());
    }

    private void recreateSnapshots() {
        this.snapshot = List.copyOf(this.items);
        this.itemIds = IntSet.of(this.items.stream().mapToInt(BoxItem::getInternalId).toArray());
    }
}
