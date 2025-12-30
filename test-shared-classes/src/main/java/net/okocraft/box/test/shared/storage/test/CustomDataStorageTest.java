package net.okocraft.box.test.shared.storage.test;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.test.shared.util.NodeAssertion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class CustomDataStorageTest<S> extends AbstractStorageTest<S> {

    private static final Key KEY_1 = Key.key("box", "test");
    private static final Key KEY_2 = Key.key("box", "path/to/test");
    private static final Key KEY_3 = Key.key("unbox", "test");

    private static void testVisit(@NotNull CustomDataStorage storage, boolean saveNodes) throws Exception {
        MapNode node = mapNode();

        if (saveNodes) {
            storage.saveData(KEY_1, node);
            storage.saveData(KEY_2, node);
            storage.saveData(KEY_3, node);
        }

        { // test CustomDataStorage#visitData
            Set<Key> expectedKeys = new HashSet<>(Arrays.asList(KEY_1, KEY_2));

            storage.visitData(KEY_1.namespace(), ((key, mapNode) -> {
                Assertions.assertTrue(expectedKeys.remove(key));
                NodeAssertion.assertEquals(node, mapNode);
            }));

            Assertions.assertTrue(expectedKeys.isEmpty(), expectedKeys::toString);
        }

        { // test CustomDataStorage#visitAll
            Set<Key> expectedKeys = new HashSet<>(Arrays.asList(KEY_1, KEY_2, KEY_3));

            storage.visitAllData(((key, mapNode) -> {
                Assertions.assertTrue(expectedKeys.remove(key));
                NodeAssertion.assertEquals(node, mapNode);
            }));

            Assertions.assertTrue(expectedKeys.isEmpty());
        }
    }

    private static @NotNull MapNode mapNode() {
        MapNode mapNode = MapNode.create();
        mapNode.set("string", "value");
        mapNode.set("integer", 100);
        mapNode.set("double", 3.14);
        mapNode.set("bool", true);
        mapNode.set("list", List.of("A", "B", "C"));
        mapNode.set("map", Map.of("key", "value"));
        mapNode.set("nested", Map.of("map", Map.of("key", "value")));
        return mapNode;
    }

    @Test
    void testLoadingAndSaving() throws Exception {
        S storage = this.newStorage();
        CustomDataStorage customDataStorage = this.newCustomDataStorage(storage);

        try {
            NodeAssertion.assertEquals(MapNode.empty(), customDataStorage.loadData(KEY_1));
            NodeAssertion.assertEquals(MapNode.empty(), customDataStorage.loadData(KEY_2));

            MapNode node = mapNode();

            customDataStorage.saveData(KEY_1, node);
            NodeAssertion.assertEquals(node, customDataStorage.loadData(KEY_1));

            customDataStorage.saveData(KEY_2, node);
            NodeAssertion.assertEquals(node, customDataStorage.loadData(KEY_2));

            MapNode newNode = MapNode.create(Map.of("a", "b"));
            customDataStorage.saveData(KEY_2, newNode); // overwrite
            NodeAssertion.assertEquals(newNode, customDataStorage.loadData(KEY_2));
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testVisit() throws Exception {
        S storage = this.newStorage();
        CustomDataStorage customDataStorage = this.newCustomDataStorage(storage);

        try {
            testVisit(customDataStorage, true);
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testConvert() throws Exception {
        S storage = this.newStorage();

        try {
            Optional<CustomDataStorage> legacy = this.newLegacyCustomDataStorage(storage);

            if (legacy.isEmpty()) {
                return;
            }

            CustomDataStorageTest.testVisit(legacy.get(), true);

            CustomDataStorage table = this.newCustomDataStorage(storage);
            table.updateFormatIfNeeded();
            CustomDataStorageTest.testVisit(table, false);
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull CustomDataStorage newCustomDataStorage(@NotNull S storage) throws Exception;

    protected @NotNull Optional<CustomDataStorage> newLegacyCustomDataStorage(@NotNull S storage) throws Exception {
        return Optional.empty();
    }

}
