package net.okocraft.box.storage.api.loader.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.NamedItem;
import net.okocraft.box.storage.api.util.SneakyThrow;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

record DefaultItemLoader<I extends NamedItem<?>>(@NotNull Stream<I> defaultItemStream,
                                                 @NotNull DefaultItemStorage itemStorage) {

    <R> @NotNull List<R> initialize(@NotNull BiFunction<I, Integer, R> function) throws Exception {
        return this.itemStorage.initializeDefaultItems(this.defaultItemStream, function);
    }

    <R> @NotNull List<R> load(@NotNull BiFunction<I, Integer, R> function, @NotNull Consumer<I> unknownItemConsumer) throws Exception {
        var nameToIdMap = this.itemStorage.loadDefaultItemNameToIdMap();

        return this.defaultItemStream.map(item -> {
            if (nameToIdMap.containsKey(item.plainName())) {
                return function.apply(item, nameToIdMap.removeInt(item.plainName()));
            } else {
                unknownItemConsumer.accept(item);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    <R> @NotNull UpdateResult<R> update(@NotNull Map<String, String> renamedItems, @NotNull BiFunction<I, Integer, R> function) throws Exception {
        var nameToIdMap = this.itemStorage.loadDefaultItemNameToIdMap();

        var remappedItems = new Int2ObjectOpenHashMap<RemappedItem>();
        var toRename = new Int2ObjectOpenHashMap<String>();

        for (var entry : renamedItems.entrySet()) {
            if (nameToIdMap.containsKey(entry.getValue())) {
                if (nameToIdMap.containsKey(entry.getKey())) {
                    int id = nameToIdMap.removeInt(entry.getKey());
                    remappedItems.put(
                        id,
                        new RemappedItem(id, entry.getKey(), nameToIdMap.getInt(entry.getValue()))
                    );
                } else {
                    BoxLogger.logger().warn("Unknown renamed item found: {} (Not exists) -> {} (Exists)", entry.getKey(), entry.getValue());
                }
            } else if (nameToIdMap.containsKey(entry.getKey())) {
                int id = nameToIdMap.removeInt(entry.getKey());
                nameToIdMap.put(entry.getValue(), id);
                toRename.put(id, entry.getValue());
            } else {
                BoxLogger.logger().warn("Unknown renamed item found: {} (Exists) -> {} (Not Exists)", entry.getKey(), entry.getValue());
            }
        }

        this.itemStorage.removeItems(remappedItems.keySet());
        this.itemStorage.renameItems(toRename);

        return new UpdateResult<>(this.defaultItemStream.map(item -> {
            if (nameToIdMap.containsKey(item.plainName())) {
                return function.apply(item, nameToIdMap.removeInt(item.plainName()));
            } else {
                try {
                    return function.apply(item, this.itemStorage.newDefaultItemId(item.plainName()));
                } catch (Exception e) {
                    SneakyThrow.sneaky(e);
                    throw new AssertionError();
                }
            }
        }).filter(Objects::nonNull).toList(), remappedItems.values());
    }

    record RemappedItem(int oldId, @NotNull String name, int newId) {
    }

    record UpdateResult<R>(@NotNull List<R> items, @NotNull Collection<RemappedItem> remappedItems) {
    }
}
