package net.okocraft.box.plugin.gui.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractMenuItem implements MenuItem {

    protected final ItemStack item;
    protected final int index;

    public AbstractMenuItem(@NotNull ItemStack item, int index) {
        this.item = item;
        this.index = index;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return item;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractMenuItem)) {
            return false;
        }

        AbstractMenuItem that = (AbstractMenuItem) o;
        return index == that.index && item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, index);
    }
}
