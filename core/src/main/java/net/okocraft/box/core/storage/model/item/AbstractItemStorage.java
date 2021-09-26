package net.okocraft.box.core.storage.model.item;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.ItemImportEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.core.model.item.BoxCustomItemImpl;
import net.okocraft.box.core.model.item.BoxItemImpl;
import net.okocraft.box.core.util.DefaultItemProvider;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractItemStorage implements ItemStorage {

    protected AbstractItemStorage() {
    }

    @Override
    public @NotNull BoxCustomItem registerNewItem(@NotNull ItemStack original) throws Exception {
        if (isRegisteredItem(original)) {
            throw new IllegalArgumentException("the same item is already registered.");
        }

        int id = getNewItemId();
        var plainName = original.getType().name() + "#" + id;
        var item = new BoxCustomItemImpl(original, plainName, id);

        saveNewCustomItem(item);
        saveVersionedItem(Bukkit.getMinecraftVersion(), item);

        return item;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> loadAllItems() throws Exception {
        var itemList = new ArrayList<BoxItem>(500); // current: 1308 items

        onLoadingAllItemsStarted();

        processDefaultItems(DefaultItemProvider.getDefaultItems(), itemList);

        processDefaultItems(DefaultItemProvider.getDefaultPotions(), itemList);

        processDefaultItems(DefaultItemProvider.getDefaultEnchantedBooks(), itemList);

        processDefaultItems(DefaultItemProvider.getDefaultFireworks(), itemList);

        var defaultItems = itemList.size();
        BoxProvider.get().getLogger().info(defaultItems + " default items imported!");

        var customItems = loadCustomItems();

        processCustomItems(customItems, itemList);

        BoxProvider.get().getLogger().info(customItems.size() + " custom items imported!");

        saveVersionedItems(Bukkit.getMinecraftVersion(), itemList);

        updateCustomItems(customItems);

        onLoadingAllItemsFinished();

        return itemList;
    }

    private void processDefaultItems(@NotNull List<DefaultItemProvider.DefaultItem> defaultItems,
                                     @NotNull List<BoxItem> itemList) {
        var logger = BoxProvider.get().getLogger();

        for (var item : defaultItems) {
            int id;

            try {
                id = getDefaultItemId(item.plainName());
            } catch (Exception e) {
                logger.log(
                        Level.WARNING,
                        "Could not get default item id, ignore it... (" + item.plainName() + ")",
                        e
                );
                continue;
            }

            var boxItem = new BoxItemImpl(item.itemStack(), item.plainName(), id);

            if (checkItem(boxItem, itemList)) {
                itemList.add(boxItem);
                BoxProvider.get().getEventBus()
                        .callEvent(new ItemImportEvent(boxItem, ItemImportEvent.ItemType.DEFAULT_ITEM));
            }
        }
    }

    private void processCustomItems(@NotNull List<BoxCustomItem> customItems, @NotNull List<BoxItem> itemList) {
        for (var item : customItems) {
            if (checkItem(item, itemList)) {
                itemList.add(item);
                BoxProvider.get().getEventBus()
                        .callEvent(new ItemImportEvent(item, ItemImportEvent.ItemType.CUSTOM_ITEM));
            }
        }
    }

    private boolean checkItem(@NotNull BoxItem toCheck, @NotNull List<BoxItem> itemList) {
        var logger = BoxProvider.get().getLogger();

        for (var item : itemList) {
            if (item.getInternalId() == toCheck.getInternalId()) {
                logger.warning("the internal id already exists (" + item.getPlainName() + ")");
                return false;
            }

            if (item.getPlainName().equals(toCheck.getPlainName())) {
                logger.warning("the plain name already exists (" + item.getPlainName() + ")");
                return false;
            }

            if (item.getOriginal().isSimilar(toCheck.getOriginal())) {
                logger.warning("the same item already exists (" + item.getPlainName() + ")");
                return false;
            }
        }

        return true;
    }

    protected abstract void onLoadingAllItemsStarted() throws Exception;

    protected abstract void onLoadingAllItemsFinished() throws Exception;

    protected abstract int getNewItemId() throws Exception;

    protected abstract int getDefaultItemId(@NotNull String name) throws Exception;

    protected abstract @NotNull List<BoxCustomItem> loadCustomItems() throws Exception;

    protected abstract void updateCustomItems(@NotNull List<BoxCustomItem> customItems) throws Exception;

    protected abstract void saveNewCustomItem(@NotNull BoxCustomItem item) throws Exception;

    protected abstract void saveVersionedItem(@NotNull String version, @NotNull BoxItem item) throws Exception;

    protected abstract void saveVersionedItems(@NotNull String version,
                                               @NotNull Collection<BoxItem> defaultItems) throws Exception;
}
