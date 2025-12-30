package net.okocraft.box.core.model.loader.state;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PerItemChangeStateTest extends AbstractChangeStateTest {

    @Test
    void test() throws Exception {
        MemoryPartialSavingStockStorage storage = new MemoryPartialSavingStockStorage();
        StockHolder stockHolder = TestStockHolder.create(STOCK_DATA);
        PerItemChangeState state = new PerItemChangeState(storage);

        checkSize(state, 0);

        state.rememberChange(ITEM_ID);

        checkSize(state, 1);

        state.saveChanges(stockHolder);

        checkSize(state, 0);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        state.rememberReset(stockHolder.reset());

        checkSize(state, 1);

        state.saveChanges(stockHolder);

        checkSize(state, 0);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    @Override
    protected @NotNull ChangeState createState() {
        return new PerItemChangeState(new MemoryPartialSavingStockStorage());
    }

    private void checkSize(@NotNull PerItemChangeState state, int expected) {
        if (expected == 0) {
            Assertions.assertTrue(state.getChangedItemIds().isEmpty());
        } else {
            Assertions.assertEquals(expected, state.getChangedItemIds().size());
        }
    }
}
