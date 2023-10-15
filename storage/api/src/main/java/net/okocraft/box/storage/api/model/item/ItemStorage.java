package net.okocraft.box.storage.api.model.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ItemStorage {

    void init() throws Exception;

    @Nullable MCDataVersion getDataVersion() throws Exception;

    int getDefaultItemVersion() throws Exception;

    void saveItemVersion(@NotNull MCDataVersion dataVersion, int defaultItemVersion) throws Exception;

    @NotNull List<ItemData> loadAllDefaultItems() throws Exception;

    @NotNull List<BoxItem> saveDefaultItems(@NotNull List<DefaultItem> newItems, @NotNull Int2ObjectMap<DefaultItem> updatedItemMap) throws Exception;

    @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception;

    void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception;

    @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception;

    @NotNull BoxCustomItem renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception;

}
