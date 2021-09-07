package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.core.model.item.BoxCustomItemImpl;
import net.okocraft.box.core.storage.model.item.ItemStorage;
import net.okocraft.box.core.util.ExecutorProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class BoxItemManager implements ItemManager {

    private final ItemStorage itemStorage;
    private final ExecutorService executor;

    private Map<ItemStack, BoxItem> itemMap = Collections.emptyMap();
    private Set<String> itemNameCache = Collections.emptySet();

    public BoxItemManager(@NotNull ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
        this.executor = ExecutorProvider.newSingleThreadExecutor("Item-Manager");
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull ItemStack itemStack) {
        var copied = itemStack.clone();

        copied.setAmount(1);

        return Optional.ofNullable(itemMap.get(copied));
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull String name) {
        name = name.toUpperCase(Locale.ROOT);

        for (var item : itemMap.values()) {
            if (item.getPlainName().equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(int id) {
        for (var item : itemMap.values()) {
            if (item.getInternalId() == id) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isRegistered(@NotNull ItemStack itemStack) {
        var copied = itemStack.clone();

        copied.setAmount(1);

        return itemMap.containsKey(copied);
    }

    @Override
    public boolean isUsed(@NotNull String name) {
        var nameSet = itemNameCache;
        return nameSet.contains(name);
    }

    @Override
    public boolean isCustomItem(@NotNull BoxItem item) {
        return item instanceof BoxCustomItemImpl;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original) {
        return CompletableFuture.supplyAsync(() -> {
            var copied = original.clone();

            copied.setAmount(1);

            if (isRegistered(copied)) {
                throw new IllegalStateException("The item is already registered (item: " + copied + ")");
            }

            BoxCustomItem customItem;

            try {
                customItem = itemStorage.registerNewItem(copied);
            } catch (Exception e) {
                throw new RuntimeException("Could not register a new item (item: " + copied + ")", e);
            }

            itemMap.put(copied, customItem);
            updateItemNameCache();

            return customItem;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxCustomItem> renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isCustomItem(item)) {
                throw new IllegalStateException("Could not rename item because the item is created by box.");
            }

            if (itemNameCache.contains(newName)) {
                throw new IllegalStateException("The same name is already used (" + newName + ")");
            }

            var internal = (BoxCustomItemImpl) item;

            internal.setPlainName(newName);
            updateItemNameCache();

            try {
                itemStorage.saveCustomItem(internal);
            } catch (Exception e) {
                throw new RuntimeException("Could not save the custom item", e);
            }

            return internal;
        }, executor);
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getItemNameSet() {
        return itemNameCache;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getBoxItemSet() {
        return List.copyOf(itemMap.values());
    }

    public void importAllItems() throws Exception {
        itemMap =
                itemStorage.loadAllItems()
                        .stream()
                        .collect(Collectors.toConcurrentMap(BoxItem::getOriginal, item -> item));

        updateItemNameCache();
    }

    private void updateItemNameCache() {
        itemNameCache =
                itemMap.values().stream()
                        .map(BoxItem::getPlainName)
                        .collect(Collectors.toUnmodifiableSet());
    }
}
