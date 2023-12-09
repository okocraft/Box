package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PerItemChangeStateTest extends AbstractChangeStateTest {

    @Test
    void test() {
        var storage = new MemoryPartialSavingStockStorage();
        var stockHolder = createStockHolder();
        var state = new PerItemChangeState(storage, stockHolder, this::ignoreStorageError);

        checkSize(state, 0);

        state.rememberChange(ITEM_ID);

        checkSize(state, 1);

        state.saveChanges();

        checkSize(state, 0);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        stockHolder.reset();
        state.rememberReset(STOCK_DATA);

        checkSize(state, 1);

        state.saveChanges();

        checkSize(state, 0);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    private void checkSize(@NotNull PerItemChangeState state, int expected) {
        if (expected == 0) {
            Assertions.assertTrue(state.getChangedItemIds().isEmpty());
        } else {
            Assertions.assertEquals(expected, state.getChangedItemIds().size());
        }
    }
}
