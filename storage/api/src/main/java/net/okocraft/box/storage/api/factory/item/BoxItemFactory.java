package net.okocraft.box.storage.api.factory.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BoxItemFactory {

    @Contract("_,_ -> new")
    public static @NotNull BoxItem createDefaultItem(@NotNull DefaultItem defaultItem, int internalId) {
        return new BoxItemImpl(defaultItem.itemStack(), defaultItem.plainName(), internalId);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull BoxItem createDefaultItem(@NotNull ItemStack original,
                                                     @NotNull String plainName, int internalId) {
        return new BoxItemImpl(original, plainName, internalId);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull BoxCustomItem createCustomItem(@NotNull ItemStack original,
                                                          @NotNull String plainName, int internalId) {
        return new BoxCustomItemImpl(original, plainName, internalId);
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
