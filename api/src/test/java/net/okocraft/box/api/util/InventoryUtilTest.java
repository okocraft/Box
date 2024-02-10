package net.okocraft.box.api.util;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.serialization.annotation.CollectionType;
import com.github.siroshun09.configapi.core.serialization.annotation.DefaultInt;
import com.github.siroshun09.configapi.core.serialization.key.Key;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class InventoryUtilTest {

    @ParameterizedTest(name = "")
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        var contents = ArgumentCaptor.forClass(ItemStack[].class);

        int remainingAmount = InventoryUtil.putItems(
                testCase.inventory().createTestInventory(contents),
                testCase.puttingItem().toItemStack(),
                testCase.puttingItem().amount()
        );

        Assertions.assertEquals(testCase.expectedReturnValue(), remainingAmount);

        if (testCase.puttingItem().amount() != remainingAmount) {
            testCase.inventory().checkExpectedResultContents(contents);
        }
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        try (var input = InventoryUtilTest.class.getClassLoader().getResourceAsStream("inventory-util-test.yml")) {
            return input != null ? readTestCases(YamlFormat.DEFAULT.load(input)) : Collections.emptyList();
        }
    }

    private static @NotNull ItemStack createItemStack(@NotNull Material item, int amount) {
        if (amount < 1 || item.getMaxStackSize() < amount) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        var mock = Mockito.mock(ItemStack.class);
        Mockito.when(mock.getType()).thenReturn(item);
        Mockito.when(mock.getAmount()).thenReturn(amount);
        Mockito.when(mock.getMaxStackSize()).thenReturn(item.getMaxStackSize());
        Mockito.when(mock.asQuantity(Mockito.anyInt())).thenAnswer(invocation -> createItemStack(item, invocation.getArgument(0)));
        Mockito.when(mock.isSimilar(Mockito.any())).thenAnswer(invocation -> mock.getType() == invocation.<ItemStack>getArgument(0).getType());
        return mock;
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

        public @NotNull Inventory createTestInventory(@NotNull ArgumentCaptor<ItemStack[]> captor) {
            var mock = Mockito.mock(Inventory.class);
            var contents = toContents(this.size, this.initialItems);
            Mockito.when(mock.getStorageContents()).thenReturn(contents);
            Mockito.doNothing().when(mock).setStorageContents(captor.capture());
            return mock;
        }

        public void checkExpectedResultContents(@NotNull ArgumentCaptor<ItemStack[]> captor) {
            var actualContents = captor.getValue();
            Assertions.assertEquals(this.size, actualContents.length);

            var expectedContents = toContents(this.size, this.resultItems);
            for (int i = 0; i < expectedContents.length; i++) {
                var expectedItem = expectedContents[i];
                var actualItem = actualContents[i];

                if (expectedItem == null) {
                    Assertions.assertNull(actualItem);
                } else {
                    Assertions.assertEquals(expectedItem.getType(), actualItem.getType());
                    Assertions.assertEquals(expectedItem.getAmount(), actualItem.getAmount());
                }
            }
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
            return createItemStack(this.item, 1);
        }
    }

    private record InventoryItem(Material item, @DefaultInt(1) int amount, int position) {
        public @NotNull ItemStack toItemStack() {
            return createItemStack(this.item, this.amount);
        }
    }
}
