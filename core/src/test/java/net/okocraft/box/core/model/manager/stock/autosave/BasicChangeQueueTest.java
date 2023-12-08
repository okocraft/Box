package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BasicChangeQueueTest extends AbstractChangeQueueTest {

    @Test
    void test() {
        var storage = new MemoryStockStorage();
        var stockHolder = createStockHolder();
        var queue = new BasicChangeQueue(storage, stockHolder, this::ignoreStorageError);

        checkChanges(queue, false);

        queue.rememberChange(ITEM_ID);

        checkChanges(queue, true);

        queue.saveChanges();

        checkChanges(queue, false);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        stockHolder.reset();
        queue.rememberReset(STOCK_DATA);

        checkChanges(queue, true);

        queue.saveChanges();

        checkChanges(queue, false);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    private void checkChanges(@NotNull BasicChangeQueue queue, boolean expected) {
        Assertions.assertEquals(expected, queue.hasChanges());
    }

}
