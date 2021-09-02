package net.okocraft.box.core.model.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoxCustomItemImpl extends BoxItemImpl implements BoxCustomItem {

    public BoxCustomItemImpl(@NotNull ItemStack original, @NotNull String plainName, int internalId) {
        super(original, plainName, internalId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (BoxCustomItemImpl) o;
        return getInternalId() == that.getInternalId() &&
                getOriginal().equals(that.getOriginal()) &&
                getPlainName().equals(that.getPlainName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOriginal(), getPlainName(), getInternalId());
    }

    @Override
    public String toString() {
        return "BoxCustomItemImpl{" +
                "original=" + getOriginal() +
                ", plainName='" + getPlainName() + '\'' +
                ", internalId=" + getPlainName() +
                '}';
    }
}
