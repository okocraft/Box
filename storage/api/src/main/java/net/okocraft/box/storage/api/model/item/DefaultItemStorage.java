package net.okocraft.box.storage.api.model.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface DefaultItemStorage {

    int newDefaultItemId(@NotNull String name) throws Exception;

    <I extends NamedItem<?>, R> @NotNull List<R> initializeDefaultItems(@NotNull Stream<I> defaultItemStream, @NotNull BiFunction<I, Integer, R> function) throws Exception;

    @NotNull
    Object2IntMap<String> loadDefaultItemNameToIdMap() throws Exception;

    void removeItems(@NotNull IntSet itemIds) throws Exception;

    void renameItems(@NotNull Int2ObjectMap<String> idToNewNameMap) throws Exception;

    void saveDefaultItems(@NotNull List<DefaultItemData> items) throws Exception;

}
