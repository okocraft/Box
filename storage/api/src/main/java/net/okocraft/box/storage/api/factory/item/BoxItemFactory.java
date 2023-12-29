package net.okocraft.box.storage.api.factory.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BoxItemFactory {

    @Contract("_,_ -> new")
    public static @NotNull BoxDefaultItem createDefaultItem(int internalId, @NotNull DefaultItem defaultItem) {
        return new BoxDefaultItemImpl(internalId, defaultItem.plainName(), defaultItem.itemStack());
    }

    @Contract("_ -> new")
    public static @NotNull BoxDefaultItem createDefaultItem(@NotNull ItemData itemData) {
        return new BoxDefaultItemImpl(itemData.internalId(), itemData.plainName(), ItemStack.deserializeBytes(itemData.itemData()));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull BoxCustomItem createCustomItem(int internalId, @NotNull String plainName, @NotNull ItemStack itemStack) {
        return new BoxCustomItemImpl(internalId, plainName, itemStack);
    }

    @Contract("_ -> new")
    public static @NotNull BoxCustomItem createCustomItem(@NotNull ItemData itemData) {
        return new BoxCustomItemImpl(itemData.internalId(), itemData.plainName(), ItemStack.deserializeBytes(itemData.itemData()));
    }

    public static void renameCustomItem(@NotNull BoxCustomItem customItem, @NotNull String newName) {
        if (customItem instanceof BoxCustomItemImpl impl) {
            impl.setPlainName(newName);
        } else {
            throw new IllegalArgumentException(customItem + " is not implemented by storage-api");
        }
    }

    public static boolean checkCustomItem(@NotNull BoxItem customItem) {
        return customItem instanceof BoxCustomItemImpl;
    }
}
