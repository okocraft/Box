package net.okocraft.box.storage.api.model.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemStorage {

    void init() throws Exception;

    void close() throws Exception;

    int getDataVersion() throws Exception;

    void saveCurrentDataVersion() throws Exception;

    @NotNull List<BoxItem> loadAllDefaultItems() throws Exception;

    @NotNull List<BoxItem> updateDefaultItems(@NotNull Map<BoxItem, DefaultItem> itemMap) throws Exception ;

    @NotNull List<BoxItem> saveNewDefaultItems(@NotNull List<DefaultItem> newItems) throws Exception ;

    @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception;

    void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception;

    @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item) throws Exception ;

    @NotNull BoxCustomItem rename(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception;

}
