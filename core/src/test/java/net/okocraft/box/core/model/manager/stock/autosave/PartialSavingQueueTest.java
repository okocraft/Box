package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.storage.memory.stock.MemoryPartialSavingStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PartialSavingQueueTest extends AbstractChangeQueueTest {

    @Test
    void test() {
        var storage = new MemoryPartialSavingStockStorage();
        var stockHolder = createStockHolder();
        var queue = new PartialSavingQueue(storage, stockHolder, this::ignoreStorageError);

        checkSize(queue, 0);

        queue.rememberChange(ITEM_ID);

        checkSize(queue, 1);

        queue.saveChanges();

        checkSize(queue, 0);
        this.checkStorageAmount(storage, stockHolder, ITEM_AMOUNT);

        stockHolder.reset();
        queue.rememberReset(createStockData());

        checkSize(queue, 1);

        queue.saveChanges();

        checkSize(queue, 0);
        this.checkStorageAmount(storage, stockHolder, 0);
    }

    private void checkSize(@NotNull PartialSavingQueue queue, int expected) {
        if (expected == 0) {
            Assertions.assertTrue(queue.getQueuedItemIds().isEmpty());
        } else {
            Assertions.assertEquals(expected, queue.getQueuedItemIds().size());
        }
    }
}
