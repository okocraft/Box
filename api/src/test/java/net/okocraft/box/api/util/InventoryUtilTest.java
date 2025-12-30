package net.okocraft.box.api.util;

import dev.siroshun.serialization.annotation.Inline;
import net.okocraft.box.test.shared.mock.bukkit.inventory.ContentsHoldingInventory;
import net.okocraft.box.test.shared.mock.bukkit.inventory.InventoryInfo;
import net.okocraft.box.test.shared.model.item.ItemType;
import net.okocraft.box.test.shared.util.TestCaseLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;

class InventoryUtilTest {

    @ParameterizedTest
    @MethodSource({"loadTestCases"})
    void test(TestCase testCase) {
        ContentsHoldingInventory inventory = testCase.inventory().createTestInventory();

        Assertions.assertEquals(
            testCase.expectedReturnValue(),
            InventoryUtil.putItems(inventory, testCase.item().toItemStack(1), testCase.amount())
        );

        testCase.inventory().checkContents(inventory);
    }

    private static @NotNull Collection<TestCase> loadTestCases() throws IOException {
        return TestCaseLoader.loadFromResource(TestCase.class, "inventory-util-test.yml");
    }

    private record TestCase(String name, @Inline ItemType item, int amount,
                            InventoryInfo inventory, int expectedReturnValue) {
    }
}
