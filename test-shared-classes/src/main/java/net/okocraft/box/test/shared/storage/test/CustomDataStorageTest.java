package net.okocraft.box.test.shared.storage.test;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.test.shared.util.NodeAssertion;
import net.kyori.adventure.key.Key;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class CustomDataStorageTest {

    private static final Key KEY_1 = Key.key("box", "test");
    private static final Key KEY_2 = Key.key("box", "path/to/test");
    private static final Key KEY_3 = Key.key("unbox", "test");

    public static void testLoadingAndSaving(@NotNull CustomDataStorage storage) throws Exception {
        storage.init();

        NodeAssertion.assertEquals(MapNode.empty(), storage.loadData(KEY_1));
        NodeAssertion.assertEquals(MapNode.empty(), storage.loadData(KEY_2));

        var node = mapNode();

        storage.saveData(KEY_1, node);
        NodeAssertion.assertEquals(node, storage.loadData(KEY_1));

        storage.saveData(KEY_2, node);
        NodeAssertion.assertEquals(node, storage.loadData(KEY_2));

        var newNode = MapNode.create(Map.of("a", "b"));
        storage.saveData(KEY_2, newNode); // overwrite
        NodeAssertion.assertEquals(newNode, storage.loadData(KEY_2));
    }

    public static void testLoadingFromNewlyCreatedStorage(@NotNull CustomDataStorage storage) throws Exception {
        storage.init();
        NodeAssertion.assertEquals(mapNode(), storage.loadData(KEY_1));
        NodeAssertion.assertEquals(MapNode.create(Map.of("a", "b")), storage.loadData(KEY_2));
    }

    public static void testVisit(@NotNull CustomDataStorage storage, boolean saveNodes) throws Exception {
        storage.init();

        var node = mapNode();

        if (saveNodes) {
            storage.saveData(KEY_1, node);
            storage.saveData(KEY_2, node);
            storage.saveData(KEY_3, node);
        }

        { // test CustomDataStorage#visitData
            var expectedKeys = new HashSet<>(Arrays.asList(KEY_1, KEY_2));

            storage.visitData(KEY_1.namespace(), ((key, mapNode) -> {
                Assertions.assertTrue(expectedKeys.remove(key));
                NodeAssertion.assertEquals(node, mapNode);
            }));

            Assertions.assertTrue(expectedKeys.isEmpty(), expectedKeys::toString);
        }

        { // test CustomDataStorage#visitAll
            var expectedKeys = new HashSet<>(Arrays.asList(KEY_1, KEY_2, KEY_3));

            storage.visitAllData(((key, mapNode) -> {
                Assertions.assertTrue(expectedKeys.remove(key));
                NodeAssertion.assertEquals(node, mapNode);
            }));

            Assertions.assertTrue(expectedKeys.isEmpty());
        }
    }

    private static @NotNull MapNode mapNode() {
        var mapNode = MapNode.create();
        mapNode.set("string", "value");
        mapNode.set("integer", 100);
        mapNode.set("double", 3.14);
        mapNode.set("bool", true);
        mapNode.set("list", List.of("A", "B", "C"));
        mapNode.set("map", Map.of("key", "value"));
        mapNode.set("nested", Map.of("map", Map.of("key", "value")));
        return mapNode;
    }
}
