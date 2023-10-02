package net.okocraft.box.storage.api.model.item;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record ItemData(int internalId, @NotNull String plainName, byte[] itemData) {

    public @NotNull BoxItem toDefaultItem() {
        return BoxItemFactory.createDefaultItem(ItemStack.deserializeBytes(itemData), plainName, internalId);
    }

}
