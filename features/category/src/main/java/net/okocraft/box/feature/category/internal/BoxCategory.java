package net.okocraft.box.feature.category.internal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.model.Category;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class BoxCategory implements Category {

    private final String name;
    private final TranslatableComponent displayName;
    private final Material iconMaterial;

    private final List<BoxItem> items = new ArrayList<>();
    private List<BoxItem> copiedItems = null;

    BoxCategory(@NotNull String name, @NotNull Material iconMaterial) {
        this.name = name;
        this.displayName = Component.translatable("box.category.name." + name);
        this.iconMaterial = iconMaterial;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TranslatableComponent getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return iconMaterial;
    }

    @Override
    public @NotNull @Unmodifiable List<BoxItem> getItems() {
        if (copiedItems == null) {
            copiedItems = List.copyOf(items);
        }

        return copiedItems;
    }

    void add(@NotNull BoxItem item) {
        items.add(item);

        // if the items are already copied, update them.
        if (copiedItems != null) {
            copiedItems = List.copyOf(items);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxCategory that = (BoxCategory) o;
        return name.equals(that.name) && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, items);
    }

    @Override
    public String toString() {
        return "BoxCategory{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}
