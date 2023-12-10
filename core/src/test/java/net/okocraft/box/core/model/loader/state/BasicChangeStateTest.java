package net.okocraft.box.core.model.loader.state;

import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BasicChangeStateTest extends AbstractChangeStateTest {

    @Test
    void test() throws Exception {
        var storage = new MemoryStockStorage();
        var stockHolder = TestStockHolder.create(STOCK_DATA);
        var state = new BasicChangeState(storage);

        checkChanges(state, false);

        state.rememberChange(ITEM_ID);

        checkChanges(state, true);

        state.saveChanges(stockHolder);

        checkChanges(state, false);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        stockHolder.reset();
        state.rememberReset(STOCK_DATA);

        checkChanges(state, true);

        state.saveChanges(stockHolder);

        checkChanges(state, false);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    @Override
    protected @NotNull ChangeState createState() {
        return new BasicChangeState(new MemoryStockStorage());
    }

    private void checkChanges(@NotNull BasicChangeState state, boolean expected) {
        Assertions.assertEquals(expected, state.hasChanges());
    }

}
