package net.okocraft.box.plugin.category;

import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Category {

    private final String name;
    private final String displayName;
    private final ItemStack icon;
    private final List<Item> items;

    public Category(@NotNull String name, @NotNull String displayName,
                    @NotNull ItemStack icon, @NotNull List<Item> items) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.items = items;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public ItemStack getIcon() {
        return icon;
    }

    @NotNull
    public List<Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Category)) {
            return false;
        }

        Category category = (Category) o;
        return Objects.equals(name, category.name) && Objects.equals(items, category.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, items);
    }
}
