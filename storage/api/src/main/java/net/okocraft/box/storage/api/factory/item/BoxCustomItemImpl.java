package net.okocraft.box.storage.api.factory.item;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

final class BoxCustomItemImpl implements BoxCustomItem {

    private final int internalId;
    private final ItemStack original;
    private String plainName;

    BoxCustomItemImpl(int internalId, @NotNull String plainName, @NotNull ItemStack original) {
        this.internalId = internalId;
        this.original = original;
        this.plainName = plainName;
    }

    @Override
    public int getInternalId() {
        return this.internalId;
    }

    @Override
    public @NotNull String getPlainName() {
        return this.plainName;
    }

    void setPlainName(@NotNull String plainName) {
        this.plainName = Objects.requireNonNull(plainName);
    }

    @Override
    public @NotNull ItemStack getOriginal() {
        return this.original;
    }

    @Override
    public @NotNull ItemStack getClonedItem() {
        return this.original.clone();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.original.displayName();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BoxItem other && this.internalId == other.getInternalId();
    }

    @Override
    public int hashCode() {
        return this.internalId;
    }
}
