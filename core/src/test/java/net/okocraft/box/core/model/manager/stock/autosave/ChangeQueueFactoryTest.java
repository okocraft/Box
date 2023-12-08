package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeQueueFactoryTest extends AbstractChangeQueueTest {

    @Test
    void testBasic() {
        var factory = ChangeQueue.createFactory(new MemoryStockStorage(), this::ignoreStorageError);
        Assertions.assertEquals(BasicChangeQueue.class, factory.createQueue(createStockHolder()).getClass());
    }

    @Test
    void testPartial() {
        var factory = ChangeQueue.createFactory(new MemoryPartialSavingStockStorage(), this::ignoreStorageError);
        Assertions.assertEquals(PartialSavingQueue.class, factory.createQueue(createStockHolder()).getClass());
    }

}
