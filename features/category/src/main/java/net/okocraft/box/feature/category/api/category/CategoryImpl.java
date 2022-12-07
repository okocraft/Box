package net.okocraft.box.feature.category.api.category;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CategoryImpl implements Category {

    private final Component displayName;
    private final Material iconMaterial;
    private final boolean shouldSave;

    private final List<BoxItem> items = Collections.synchronizedList(new ArrayList<>());
    private List<BoxItem> snapshot = null;

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
            snapshot = List.copyOf(items);
        }

        return snapshot;
    }

    @Override
    public void addItem(@NotNull BoxItem item) {
        items.add(item);
        updateSnapshot();
    }

    @Override
    public void removeItem(@NotNull BoxItem item) {
        items.remove(item);
        updateSnapshot();
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
    }
}
