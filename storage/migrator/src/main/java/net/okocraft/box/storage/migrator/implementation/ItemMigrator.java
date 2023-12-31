package net.okocraft.box.storage.migrator.implementation;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class ItemMigrator extends AbstractDataMigrator<ItemMigrator.Result, ItemStorage> {

    private final UserMigrator.Result userMigratorResult;
    private final DefaultItemProvider defaultItemProvider;

    public ItemMigrator(@NotNull UserMigrator.Result userMigratorResult, @NotNull DefaultItemProvider defaultItemProvider) {
        this.userMigratorResult = userMigratorResult;
        this.defaultItemProvider = defaultItemProvider;
    }

    @Override
    protected @NotNull ItemStorage getDataStorage(@NotNull Storage storage) {
        return storage.getItemStorage();
    }

    @Override
    protected @NotNull ItemMigrator.Result migrateData(@NotNull ItemStorage source, @NotNull ItemStorage target, boolean debug) throws Exception {
        var sourceItemVersion = source.getItemVersion();
        var targetItemVersion = target.getItemVersion();

        if (sourceItemVersion.isEmpty()) {
            throw new IllegalStateException("Cannot get the item version from the source storage.");
        }

        if (targetItemVersion.isEmpty()) {
            throw new IllegalStateException("Cannot get the item version from the target storage.");
        }

        if (sourceItemVersion.get().isAfter(targetItemVersion.get())) {
            throw new IllegalStateException("Cannot migrate item data to lower version.");
        }

        var patcherFactory = this.defaultItemProvider.itemNamePatcherFactory();
        var currentVersion = this.defaultItemProvider.version();
        var sourceItemIdToNameMap = loadSourceDefaultItemIdToNameMap(source, patcherFactory.create(sourceItemVersion.get(), currentVersion));
        var targetItemNameToIdMap = loadTargetDefaultItemIdToNameMap(target, patcherFactory.create(targetItemVersion.get(), currentVersion));

        var itemIdMap = new Int2IntOpenHashMap();

        for (var entry : sourceItemIdToNameMap.int2ObjectEntrySet()) {
            int idInTarget = targetItemNameToIdMap.getOrDefault(entry.getValue(), Integer.MIN_VALUE);

            if (idInTarget != Integer.MIN_VALUE) {
                itemIdMap.put(entry.getIntKey(), idInTarget);
                if (debug) {
                    logMigrated(entry.getValue(), entry.getIntKey(), idInTarget);
                }
            } else {
                BoxLogger.logger().warn("Unknown item: {} (id: {})", entry.getValue(), entry.getIntKey());
            }
        }

        var sourceCustomItems = source.loadAllCustomItems(Function.identity());
        var targetCustomItems = this.loadTargetCustomItems(target);
        int migrated = 0;

        for (var customItem : sourceCustomItems) {
            var itemStack = ItemStack.deserializeBytes(customItem.itemData());
            int idInTarget = targetCustomItems.left().getOrDefault(itemStack, Integer.MIN_VALUE);

            if (idInTarget != Integer.MIN_VALUE) {
                itemIdMap.put(customItem.internalId(), idInTarget);
                if (debug) {
                    logMigrated(customItem.plainName(), customItem.internalId(), idInTarget);
                }
            } else {
                var itemName = customItem.plainName();
                var newCustomItem = target.saveNewCustomItem(itemStack, targetCustomItems.right().contains(itemName) ? itemName : null);
                itemIdMap.put(customItem.internalId(), newCustomItem.getInternalId());
                if (debug) {
                    logMigrated(customItem.plainName(), customItem.internalId(), newCustomItem.getInternalId());
                }
                migrated++;
            }
        }

        if (0 < migrated) {
            BoxLogger.logger().info("{} items are migrated.", migrated);
        }

        return new Result(this.userMigratorResult.users(), itemIdMap);
    }

    private static @NotNull Int2ObjectMap<String> loadSourceDefaultItemIdToNameMap(@NotNull ItemStorage storage, @NotNull ItemNamePatcher patcher) throws Exception {
        var loaded = storage.loadAllDefaultItems(Function.identity());
        var map = new Int2ObjectOpenHashMap<String>(loaded.size());

        for (var data : loaded) {
            map.put(data.internalId(), patcher.renameIfNeeded(data.plainName()));
        }

        return map;
    }

    private static @NotNull Object2IntMap<String> loadTargetDefaultItemIdToNameMap(@NotNull ItemStorage storage, @NotNull ItemNamePatcher patcher) throws Exception {
        var loaded = storage.loadAllDefaultItems(Function.identity());
        var map = new Object2IntOpenHashMap<String>(loaded.size());

        for (var data : loaded) {
            map.put(patcher.renameIfNeeded(data.plainName()), data.internalId());
        }

        return map;
    }

    private @NotNull Pair<Object2IntMap<ItemStack>, Set<String>> loadTargetCustomItems(@NotNull ItemStorage storage) throws Exception {
        var loaded = storage.loadAllCustomItems(Function.identity());
        var itemStackToIdMap = new Object2IntOpenHashMap<ItemStack>(loaded.size());
        var itemNameSet = new ObjectOpenHashSet<String>(loaded.size());

        for (var item : loaded) {
            itemStackToIdMap.put(ItemStack.deserializeBytes(item.itemData()), item.internalId());
            itemNameSet.add(item.plainName());
        }

        return Pair.of(itemStackToIdMap, itemNameSet);
    }

    public record Result(@NotNull Collection<BoxUser> users, @NotNull Int2IntMap itemIdMap) {
    }

    private static void logMigrated(@NotNull String itemName, int oldId, int newId) {
        BoxLogger.logger().info("{}: {} -> {}", itemName, oldId, newId);
    }
}
