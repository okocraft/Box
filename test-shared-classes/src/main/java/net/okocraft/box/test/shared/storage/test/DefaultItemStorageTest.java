package net.okocraft.box.test.shared.storage.test;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.NamedItem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class DefaultItemStorageTest<S> extends AbstractStorageTest<S> {

    private static @NotNull Stream<NamedItem<String>> defaultItemStream() {
        return IntStream.rangeClosed(1, 3).mapToObj(id -> new FakeNamedItem("item_" + id));
    }

    private static @NotNull Object2IntOpenHashMap<Object> nameToIdMap(List<RegisteredItem> items) {
        Object2IntOpenHashMap<Object> expectedItemMap = new Object2IntOpenHashMap<>();
        items.forEach(item -> expectedItemMap.put(item.item().plainName(), item.id()));
        return expectedItemMap;
    }

    @Test
    void testEmpty() throws Exception {
        S storage = this.newStorage();
        DefaultItemStorage defaultItemStorage = this.newDefaultItemStorage(storage);

        try {
            Assertions.assertTrue(defaultItemStorage.loadDefaultItemNameToIdMap().isEmpty());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testInitialize() throws Exception {
        S storage = this.newStorage();
        DefaultItemStorage defaultItemStorage = this.newDefaultItemStorage(storage);

        try {
            List<RegisteredItem> items = defaultItemStorage.initializeDefaultItems(defaultItemStream(), RegisteredItem::new);

            Assertions.assertEquals(3, items.size());

            List<NamedItem<String>> expectedItems = defaultItemStream().collect(Collectors.toList());
            Assertions.assertTrue(items.stream().map(RegisteredItem::item).allMatch(expectedItems::remove));

            Assertions.assertEquals(nameToIdMap(items), defaultItemStorage.loadDefaultItemNameToIdMap());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testNewItem() throws Exception {
        S storage = this.newStorage();
        DefaultItemStorage defaultItemStorage = this.newDefaultItemStorage(storage);

        try {
            Object2IntMap<Object> expectedItemMap = nameToIdMap(defaultItemStorage.initializeDefaultItems(defaultItemStream(), RegisteredItem::new));

            expectedItemMap.put("new_item", defaultItemStorage.newDefaultItemId("new_item"));

            Assertions.assertEquals(expectedItemMap, defaultItemStorage.loadDefaultItemNameToIdMap());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testRemoveItem() throws Exception {
        S storage = this.newStorage();
        DefaultItemStorage defaultItemStorage = this.newDefaultItemStorage(storage);

        try {
            Object2IntMap<Object> expectedItemMap = nameToIdMap(defaultItemStorage.initializeDefaultItems(defaultItemStream(), RegisteredItem::new));

            int id = expectedItemMap.removeInt("item_2");
            defaultItemStorage.removeItems(IntSet.of(id));

            Assertions.assertEquals(expectedItemMap, defaultItemStorage.loadDefaultItemNameToIdMap());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testRenameItem() throws Exception {
        S storage = this.newStorage();
        DefaultItemStorage defaultItemStorage = this.newDefaultItemStorage(storage);

        try {
            Object2IntMap<Object> expectedItemMap = nameToIdMap(defaultItemStorage.initializeDefaultItems(defaultItemStream(), RegisteredItem::new));

            int id = expectedItemMap.removeInt("item_2");
            expectedItemMap.put("renamed_item", id);

            defaultItemStorage.renameItems(new Int2ObjectArrayMap<>(new int[]{id}, new String[]{"renamed_item"}));

            Assertions.assertEquals(expectedItemMap, defaultItemStorage.loadDefaultItemNameToIdMap());
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull DefaultItemStorage newDefaultItemStorage(@NotNull S storage) throws Exception;

    private record FakeNamedItem(@NotNull String plainName) implements NamedItem<String> {
        @Override
        public @NotNull String item() {
            return this.plainName;
        }
    }

    private record RegisteredItem(@NotNull NamedItem<String> item, int id) {
    }
}
