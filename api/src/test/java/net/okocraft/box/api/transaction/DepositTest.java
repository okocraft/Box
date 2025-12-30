package net.okocraft.box.api.transaction;

import dev.siroshun.serialization.annotation.Inline;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.test.shared.event.StockEventCollector;
import net.okocraft.box.test.shared.mock.bukkit.inventory.ContentsHoldingInventory;
import net.okocraft.box.test.shared.mock.bukkit.inventory.InventoryInfo;
import net.okocraft.box.test.shared.model.item.ItemType;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.util.TestCaseLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

class DepositTest {

    @ParameterizedTest
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        StockHolder stockHolder = TestStockHolder.create();
        ContentsHoldingInventory inventory = testCase.inventory().createTestInventory();
        BoxItem item = testCase.item().asBoxItem(1);

        List<TransactionResult> resultList = StockHolderTransaction.create(stockHolder).deposit(item, testCase.limit()).fromInventory(inventory, StockEventCollector.TEST_CAUSE);

        int expectedDepositedAmount = testCase.expectedDepositedAmount();

        if (expectedDepositedAmount == 0) {
            Assertions.assertTrue(resultList.isEmpty());
        } else {
            TransactionResult result = resultList.getFirst();
            Assertions.assertEquals(item, result.item());
            Assertions.assertEquals(expectedDepositedAmount, result.amount());
        }

        Assertions.assertEquals(expectedDepositedAmount, stockHolder.getAmount(1));
        testCase.inventory().checkContents(inventory);
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        return TestCaseLoader.loadFromResource(TestCase.class, "deposit-test.yml");
    }

    private record TestCase(String name, @Inline ItemType item, int limit,
                            InventoryInfo inventory, int expectedDepositedAmount) {
        @Override
        public int limit() {
            return this.limit < 1 ? Integer.MAX_VALUE : this.limit;
        }
    }
}
