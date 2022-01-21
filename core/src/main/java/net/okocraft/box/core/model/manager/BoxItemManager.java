package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
        this.executor = InternalExecutors.newSingleThreadExecutor("Item Manager");
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull ItemStack itemStack) {
        var copied = Objects.requireNonNull(itemStack).clone();

        copied.setAmount(1);

        return Optional.ofNullable(itemMap.get(copied));
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull String name) {
        name = Objects.requireNonNull(name).toUpperCase(Locale.ROOT);

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
        var copied = Objects.requireNonNull(itemStack).clone();

        copied.setAmount(1);

        return itemMap.containsKey(copied);
    }

    @Override
    public boolean isUsedName(@NotNull String name) {
        Objects.requireNonNull(name);

        var nameSet = itemNameCache;
        return nameSet.contains(name);
    }

    @Override
    public boolean isCustomItem(@NotNull BoxItem item) {
        return BoxItemFactory.checkCustomItem(item);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original) {
        Objects.requireNonNull(original);

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

            BoxProvider.get().getEventBus().callEventAsync(new CustomItemRegisterEvent(customItem));

            return customItem;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxCustomItem> renameCustomItem(@NotNull BoxCustomItem item,
                                                                               @NotNull String newName) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(newName);

        return CompletableFuture.supplyAsync(() -> {
            if (!BoxItemFactory.checkCustomItem(item)) {
                throw new IllegalStateException("Could not rename item because the item is created by box.");
            }

            if (itemNameCache.contains(newName)) {
                throw new IllegalStateException("The same name is already used (" + newName + ")");
            }

            var copy = BoxItemFactory.createCustomItem(item.getOriginal(), newName, item.getInternalId());

            try {
                itemStorage.saveCustomItem(copy);
            } catch (Exception e) {
                throw new RuntimeException("Could not save the custom item", e);
            }

            var previousName = item.getPlainName();
            BoxItemFactory.renameCustomItem(item, newName);

            updateItemNameCache();

            BoxProvider.get().getEventBus().callEventAsync(new CustomItemRenameEvent(item, previousName));

            return item;
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
