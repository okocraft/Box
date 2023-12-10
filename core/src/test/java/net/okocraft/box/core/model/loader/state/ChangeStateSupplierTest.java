package net.okocraft.box.core.model.loader.state;

import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeStateSupplierTest {

    @Test
    void testBasic() {
        var factory = ChangeState.createSupplier(new MemoryStockStorage());
        Assertions.assertInstanceOf(BasicChangeState.class, factory.get());
    }

    @Test
    void testPartial() {
        var factory = ChangeState.createSupplier(new MemoryPartialSavingStockStorage());
        Assertions.assertInstanceOf(PerItemChangeState.class, factory.get());
    }

}
