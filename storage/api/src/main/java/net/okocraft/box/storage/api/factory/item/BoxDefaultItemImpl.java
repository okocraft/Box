package net.okocraft.box.storage.api.factory.item;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

record BoxDefaultItemImpl(int internalId, @NotNull String plainName,
                          @NotNull ItemStack original) implements BoxDefaultItem {

    @Override
    public int getInternalId() {
        return this.internalId;
    }

    @Override
    public @NotNull String getPlainName() {
        return this.plainName;
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
