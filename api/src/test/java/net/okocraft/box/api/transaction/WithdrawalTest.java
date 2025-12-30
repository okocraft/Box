package net.okocraft.box.api.transaction;

import dev.siroshun.serialization.annotation.Inline;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
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

class WithdrawalTest {

    @ParameterizedTest
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        int initialAmount = testCase.initialAmount();
        StockHolder stockHolder = TestStockHolder.create(List.of(new StockData(1, initialAmount)));
        ContentsHoldingInventory inventory = testCase.inventory().createTestInventory();
        BoxItem item = testCase.item().asBoxItem(1);

        TransactionResult result = StockHolderTransaction.create(stockHolder).withdraw(item, testCase.limit()).toInventory(inventory, StockEventCollector.TEST_CAUSE);

        int expectedWithdrawnAmount = testCase.expectedWithdrawnAmount();
        Assertions.assertEquals(item, result.item());
        Assertions.assertEquals(expectedWithdrawnAmount, result.amount());

        Assertions.assertEquals(initialAmount - expectedWithdrawnAmount, stockHolder.getAmount(1));
        testCase.inventory().checkContents(inventory);
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        return TestCaseLoader.loadFromResource(TestCase.class, "withdrawal-test.yml");
    }

    private record TestCase(String name, @Inline ItemType item, int initialAmount, int limit,
                            InventoryInfo inventory, int expectedWithdrawnAmount) {
        @Override
        public int limit() {
            return this.limit < 1 ? Integer.MAX_VALUE : this.limit;
        }
    }
}
