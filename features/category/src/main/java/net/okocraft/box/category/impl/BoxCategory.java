package net.okocraft.box.category.impl;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.category.model.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class BoxCategory implements Category {

    private final String name;
    private final List<BoxItem> items = new ArrayList<>();

    BoxCategory(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull @Unmodifiable List<BoxItem> getItems() {
        return List.copyOf(items);
    }

    public void add(@NotNull BoxItem item) {
        items.add(item);
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
