package net.okocraft.box.core.model.manager;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import net.okocraft.box.core.util.executor.InternalExecutors;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

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
        return Optional.ofNullable(itemNameMap.get(Objects.requireNonNull(name)));
    }

    @Override
    public @NotNull Optional<BoxItem> getBoxItem(int id) {
        return Optional.ofNullable(itemIdMap.get(id));
    }

    @Override
    public @Nullable BoxItem getBoxItemOrNull(int id) {
        return itemIdMap.get(id);
    }

    @Override
    public @NotNull IntImmutableList getItemIdList() {
        return new IntImmutableList(itemIdMap.keySet());
    }

    @Override
    public @NotNull ObjectImmutableList<String> getItemNameList() {
        return new ObjectImmutableList<>(itemNameMap.keySet());
    }

    @Override
    public @NotNull ObjectImmutableList<BoxItem> getItemList() {
        return new ObjectImmutableList<>(itemIdMap.values());
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
    public void registerCustomItem(@NotNull ItemStack original, @Nullable String plainName, @NotNull Consumer<ItemRegistrationResult> resultConsumer) {
        var one = original.asOne();
        Objects.requireNonNull(resultConsumer);

        executor.execute(() -> {
            if (isRegistered(one)) {
                resultConsumer.accept(new ItemRegistrationResult.DuplicateItem(one));
                return;
            }

            if (plainName != null && isUsedName(plainName)) {
                resultConsumer.accept(new ItemRegistrationResult.DuplicateName(plainName));
                return;
            }

            BoxCustomItem customItem;

            try {
                customItem = itemStorage.saveNewCustomItem(one, plainName);
            } catch (Exception e) {
                resultConsumer.accept(new ItemRegistrationResult.ExceptionOccurred(e));
                return;
            }

            addItem(customItem);

            BoxProvider.get().getEventBus().callEventAsync(new CustomItemRegisterEvent(customItem));
            resultConsumer.accept(new ItemRegistrationResult.Success(customItem));
        });
    }

    @Override
    public void renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName, @NotNull Consumer<ItemRegistrationResult> resultConsumer) {
        if (!BoxItemFactory.checkCustomItem(item)) {
            throw new IllegalArgumentException("Could not rename item because the item is not created by box.");
        }

        executor.execute(() -> {
            if (itemNameMap.containsKey(newName)) {
                resultConsumer.accept(new ItemRegistrationResult.DuplicateName(newName));
                return;
            }

            removeItem(item);

            var previousName = item.getPlainName();
            BoxCustomItem result;

            try {
                result = itemStorage.rename(item, newName);
            } catch (Exception e) {
                resultConsumer.accept(new ItemRegistrationResult.ExceptionOccurred(e));
                return;
            }

            addItem(item);

            BoxProvider.get().getEventBus().callEventAsync(new CustomItemRenameEvent(result, previousName));
            resultConsumer.accept(new ItemRegistrationResult.Success(result));
        });
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getItemNameSet() {
        return Collections.unmodifiableSet(itemNameMap.keySet());
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getBoxItemSet() {
        return getItemList();
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
