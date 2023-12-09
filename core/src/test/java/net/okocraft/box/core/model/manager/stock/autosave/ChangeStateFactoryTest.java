package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeStateFactoryTest extends AbstractChangeStateTest {

    @Test
    void testBasic() {
        var factory = ChangeState.createFactory(new MemoryStockStorage(), this::ignoreStorageError);
        Assertions.assertInstanceOf(BasicChangeState.class, factory.create(this.createStockHolder()));
    }

    @Test
    void testPartial() {
        var factory = ChangeState.createFactory(new MemoryPartialSavingStockStorage(), this::ignoreStorageError);
        Assertions.assertInstanceOf(PerItemChangeState.class, factory.create(this.createStockHolder()));
    }

}
