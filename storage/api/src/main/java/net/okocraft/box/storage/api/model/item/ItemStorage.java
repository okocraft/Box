package net.okocraft.box.storage.api.model.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    void init() throws Exception;

    @NotNull Optional<ItemVersion> getItemVersion() throws Exception;

    void saveItemVersion(@NotNull ItemVersion itemVersion) throws Exception;

    @NotNull List<ItemData> loadAllDefaultItems() throws Exception;

    @NotNull List<BoxItem> saveDefaultItems(@NotNull List<DefaultItem> newItems, @NotNull Int2ObjectMap<DefaultItem> updatedItemMap) throws Exception;

    @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception;

    void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception;

    @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception;

    @NotNull BoxCustomItem renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception;

}
