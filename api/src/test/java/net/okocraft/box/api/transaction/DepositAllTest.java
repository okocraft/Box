package net.okocraft.box.api.transaction;

import com.github.siroshun09.configapi.core.serialization.annotation.CollectionType;
import com.github.siroshun09.configapi.core.serialization.annotation.MapType;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.test.shared.event.StockEventCollector;
import net.okocraft.box.test.shared.mock.bukkit.inventory.InventoryInfo;
import net.okocraft.box.test.shared.model.item.ItemType;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.util.TestCaseLoader;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

class DepositAllTest {

    @ParameterizedTest
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        var stockHolder = TestStockHolder.create();
        var inventory = testCase.inventory().createTestInventory();

        var resultList = new DepositAllImpl(stockHolder, testCase.createFilter()).fromInventory(inventory, null, testCase.createItemManager(), StockEventCollector.TEST_CAUSE);

        var resultMap = new HashMap<>(testCase.resultMap());

        for (var result : resultList) {
            int amount = resultMap.remove(result.item().getPlainName());
            Assertions.assertEquals(amount, result.amount());
            Assertions.assertEquals(amount, stockHolder.getAmount(result.item()));
        }

        Assertions.assertTrue(resultMap.isEmpty());
        testCase.inventory().checkContents(inventory);
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        return TestCaseLoader.loadFromResource(TestCase.class, "deposit-all-test.yml");
    }

    private record TestCase(String name,
                            InventoryInfo inventory,
                            @CollectionType(String.class) List<String> boxItems,
                            @CollectionType(String.class) Set<String> itemIgnores,
                            @MapType(key = String.class, value = Integer.class) Map<String, Integer> resultMap) {

        private @NotNull ItemManager createItemManager() {
            var mock = Mockito.mock(ItemManager.class);
            Mockito.when(mock.getBoxItem((ItemStack) Mockito.any())).thenAnswer(args -> {
                var type = args.<ItemStack>getArgument(0).getType();
                int index = this.boxItems.indexOf(ItemNameGenerator.key(type));
                return Optional.ofNullable(index != -1 ? new ItemType(type, type.getMaxStackSize()).asBoxItem(index) : null);
            });
            return mock;
        }

        private @NotNull Predicate<BoxItem> createFilter() {
            return item -> !this.itemIgnores.contains(item.getPlainName());
        }
    }
}
