package net.okocraft.box.storage.api.factory.item;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class BoxItemImpl implements BoxItem {

    private final ItemStack original;
    private final String plainName;
    private final int internalId;

    public BoxItemImpl(@NotNull ItemStack original, @NotNull String plainName, int internalId) {
        this.original = Objects.requireNonNull(original);
        this.plainName = Objects.requireNonNull(plainName);
        this.internalId = internalId;
    }

    @Override
    public @NotNull ItemStack getOriginal() {
        return original;
    }

    @Override
    public @NotNull ItemStack getClonedItem() {
        return original.clone();
    }

    @Override
    public @NotNull String getPlainName() {
        return plainName;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return original.displayName();
    }

    @Override
    public int getInternalId() {
        return internalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxItemImpl that = (BoxItemImpl) o;
        return internalId == that.internalId &&
                original.equals(that.original) &&
                plainName.equals(that.plainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(original, plainName, internalId);
    }

    @Override
    public String toString() {
        return "BoxItemImpl{" +
                "original=" + original +
                ", plainName='" + plainName + '\'' +
                ", internalId=" + internalId +
                '}';
    }
}
