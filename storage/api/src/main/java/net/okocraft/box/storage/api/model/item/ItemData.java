package net.okocraft.box.storage.api.model.item;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemData(int internalId, @NotNull String plainName, byte[] itemData) {

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ItemData itemData = (ItemData) object;
        return this.internalId == itemData.internalId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.internalId);
    }

    public @NotNull BoxItem toDefaultItem() {
        return BoxItemFactory.createDefaultItem(this.internalId, this.plainName, ItemStack.deserializeBytes(this.itemData));
    }
}
