package net.okocraft.box.api.util;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.serialization.annotation.CollectionType;
import com.github.siroshun09.configapi.core.serialization.annotation.DefaultInt;
import com.github.siroshun09.configapi.core.serialization.key.Key;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.test.shared.mock.bukkit.inventory.ContentsHoldingInventory;
import net.okocraft.box.test.shared.mock.bukkit.item.ItemStackMock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class InventoryUtilTest {

    @ParameterizedTest
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        var inventory = testCase.inventory().createTestInventory();

        int remainingAmount = InventoryUtil.putItems(inventory, testCase.puttingItem().toItemStack(), testCase.puttingItem().amount());

        Assertions.assertEquals(testCase.expectedReturnValue(), remainingAmount);

        if (testCase.puttingItem().amount() != remainingAmount) {
            testCase.inventory().checkContents(inventory);
        }
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        try (var input = InventoryUtilTest.class.getClassLoader().getResourceAsStream("inventory-util-test.yml")) {
            return input != null ? readTestCases(YamlFormat.DEFAULT.load(input)) : Collections.emptyList();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static @NotNull Collection<TestCase> readTestCases(@NotNull MapNode source) {
        var deserializer = RecordDeserializer.create(TestCase.class, KeyGenerator.CAMEL_TO_KEBAB);
        return source.getList("cases").asList(MapNode.class).stream().map(deserializer::deserialize).toList();
    }

    private record TestCase(String name, PuttingItem puttingItem, InventoryInfo inventory, int expectedReturnValue) {
    }

    private record InventoryInfo(int size,
                                 @Key("initial") @CollectionType(InventoryItem.class) List<InventoryItem> initialItems,
                                 @Key("result") @CollectionType(InventoryItem.class) List<InventoryItem> resultItems) {

        public @NotNull ContentsHoldingInventory createTestInventory() {
            return ContentsHoldingInventory.create(toContents(this.size, this.initialItems));
        }

        public void checkContents(@NotNull ContentsHoldingInventory inventory) {
            inventory.checkContents(toContents(this.size, this.resultItems));
        }

        private static @Nullable ItemStack @NotNull [] toContents(int size, List<InventoryItem> items) {
            var contents = new ItemStack[size];

            for (var item : items) {
                contents[item.position()] = item.toItemStack();
            }

            return contents;
        }
    }

    private record PuttingItem(Material item, @DefaultInt(1) int amount) {
        public @NotNull ItemStack toItemStack() {
            return ItemStackMock.createItemStack(this.item, 1);
        }
    }

    private record InventoryItem(Material item, @DefaultInt(1) int amount, int position) {
        public @NotNull ItemStack toItemStack() {
            return ItemStackMock.createItemStack(this.item, this.amount);
        }
    }
}
