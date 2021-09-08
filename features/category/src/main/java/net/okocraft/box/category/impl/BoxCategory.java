package net.okocraft.box.category.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.category.model.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class BoxCategory implements Category {

    private final String name;
    private final TranslatableComponent displayName;
    private final List<BoxItem> items = new ArrayList<>();

    BoxCategory(@NotNull String name) {
        this.name = name;
        this.displayName = Component.translatable("box.category.name." + name);
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
