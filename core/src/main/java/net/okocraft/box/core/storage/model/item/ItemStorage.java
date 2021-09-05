package net.okocraft.box.core.storage.model.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface ItemStorage {

    void init() throws Exception;

    void close() throws Exception;

    boolean isRegisteredItem(@NotNull ItemStack itemStack) throws Exception;

    boolean isUsedName(@NotNull String name) throws Exception;

    @NotNull @Unmodifiable Collection<BoxItem> loadAllItems() throws Exception;

    @NotNull BoxCustomItem registerNewItem(@NotNull ItemStack original) throws Exception;
}
