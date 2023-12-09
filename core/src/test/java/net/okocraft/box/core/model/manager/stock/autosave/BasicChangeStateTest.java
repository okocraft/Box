package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BasicChangeStateTest extends AbstractChangeStateTest {

    @Test
    void test() {
        var storage = new MemoryStockStorage();
        var stockHolder = createStockHolder();
        var state = new BasicChangeState(storage, stockHolder, this::ignoreStorageError);

        checkChanges(state, false);

        state.rememberChange(ITEM_ID);

        checkChanges(state, true);

        state.saveChanges();

        checkChanges(state, false);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        stockHolder.reset();
        state.rememberReset(STOCK_DATA);

        checkChanges(state, true);

        state.saveChanges();

        checkChanges(state, false);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    private void checkChanges(@NotNull BasicChangeState state, boolean expected) {
        Assertions.assertEquals(expected, state.hasChanges());
    }

}
