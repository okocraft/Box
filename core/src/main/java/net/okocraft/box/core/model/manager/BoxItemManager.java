package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.core.util.executor.InternalExecutors;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemStorage;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class BoxItemManager implements ItemManager {

    private final ItemStorage itemStorage;
    private final ExecutorService executor;

    private final Map<ItemStack, BoxItem> itemMap = createMap();
    private final Map<String, BoxItem> itemNameMap = createMap();
    private final Map<Integer, BoxItem> itemIdMap = createMap();

    public BoxItemManager(@NotNull ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
        this.executor = InternalExecutors.newSingleThreadExecutor("Item Manager");
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(itemMap.get(itemStack.asOne()));
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(@NotNull String name) {
        name = Objects.requireNonNull(name).toUpperCase(Locale.ROOT);
        return Optional.ofNullable(itemNameMap.get(name));
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(int id) {
        return Optional.ofNullable(itemIdMap.get(id));
    }

    @Override
    public boolean isRegistered(@NotNull ItemStack itemStack) {
        return itemMap.containsKey(itemStack.asOne());
    }

    @Override
    public boolean isUsedName(@NotNull String name) {
        Objects.requireNonNull(name);

        return itemNameMap.containsKey(name);
    }

    @Override
    public boolean isCustomItem(@NotNull BoxItem item) {
        return BoxItemFactory.checkCustomItem(item);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original) {
        Objects.requireNonNull(original);

        return CompletableFuture.supplyAsync(() -> {
            var copied = original.asOne();

            if (isRegistered(copied)) {
                throw new IllegalStateException("The item is already registered (item: " + copied + ")");
            }

            BoxCustomItem customItem;

            try {
                customItem = itemStorage.saveNewCustomItem(copied);
            } catch (Exception e) {
                throw new RuntimeException("Could not register a new item (item: " + copied + ")", e);
            }

            addItem(customItem);

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

            if (itemNameMap.containsKey(newName)) {
                throw new IllegalStateException("The same name is already used (" + newName + ")");
            }

            removeItem(item);

            var previousName = item.getPlainName();
            BoxCustomItem result;

            try {
                result = itemStorage.rename(item, newName);
            } catch (Exception e) {
                throw new RuntimeException("Could not save the custom item", e);
            }

            BoxProvider.get().getEventBus().callEventAsync(new CustomItemRenameEvent(result, previousName));

            return result;
        }, executor);
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getItemNameSet() {
        return Collections.unmodifiableSet(itemNameMap.keySet());
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getBoxItemSet() {
        return List.copyOf(itemMap.values());
    }

    public void storeItems(@NotNull Collection<? extends BoxItem> items) {
        items.forEach(this::addItem);
    }

    private void addItem(@NotNull BoxItem item) {
        itemMap.put(item.getOriginal(), item);
        itemNameMap.put(item.getPlainName(), item);
        itemIdMap.put(item.getInternalId(), item);
    }

    private void removeItem(@NotNull BoxItem item) {
        itemMap.remove(item.getOriginal());
        itemNameMap.remove(item.getPlainName());
        itemIdMap.remove(item.getInternalId());
    }

    private <K> @NotNull Map<K, BoxItem> createMap() {
        return new ConcurrentHashMap<>(300, 0.95f);
    }
}
