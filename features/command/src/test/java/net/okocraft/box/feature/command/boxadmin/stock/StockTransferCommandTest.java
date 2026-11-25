package net.okocraft.box.feature.command.boxadmin.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.StockHolderWrapper;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.model.stock.VoidStockEventCaller;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTransferCommandTest {

    private static final BoxItem ITEM_1 = new DummyItem(1, "item_1");
    private static final BoxItem ITEM_2 = new DummyItem(2, "item_2");
    private static final BoxItem ITEM_3 = new DummyItem(3, "item_3");
    private static final List<BoxItem> ITEMS = List.of(ITEM_1, ITEM_2, ITEM_3);

    private static final CommandCauses.AdminTransferOut OUT = new CommandCauses.AdminTransferOut(Mockito.mock(BoxUser.class), StockActor.CONSOLE);
    private static final CommandCauses.AdminTransferIn IN = new CommandCauses.AdminTransferIn(Mockito.mock(BoxUser.class), StockActor.CONSOLE);
    private static final CommandCauses.AdminTransferRefund REFUND = new CommandCauses.AdminTransferRefund(Mockito.mock(BoxUser.class), StockActor.CONSOLE);

    @Test
    void transferAllMovesStockAndMergesIntoDestination() {
        StockHolder from = holder();
        StockHolder to = holder();
        from.setAmount(ITEM_1, 10, OUT);
        from.setAmount(ITEM_2, 20, OUT);
        to.setAmount(ITEM_1, 5, IN);
        to.setAmount(ITEM_3, 7, IN);

        StockTransferCommand.Result result = StockTransferCommand.transferAll(from, to, OUT, IN, REFUND);

        assertEquals(new StockTransferCommand.Result(2, 0), result);
        assertEquals(0, from.getAmount(ITEM_1));
        assertEquals(0, from.getAmount(ITEM_2));
        assertEquals(15, to.getAmount(ITEM_1));
        assertEquals(20, to.getAmount(ITEM_2));
        assertEquals(7, to.getAmount(ITEM_3));
    }

    @Test
    void transferAllLeavesOverflowingAmountInSource() {
        StockHolder from = holder();
        StockHolder to = holder();
        from.setAmount(ITEM_1, 100, OUT);
        from.setAmount(ITEM_2, 30, OUT);
        to.setAmount(ITEM_1, Integer.MAX_VALUE - 40, IN);
        to.setAmount(ITEM_2, Integer.MAX_VALUE, IN);

        StockTransferCommand.Result result = StockTransferCommand.transferAll(from, to, OUT, IN, REFUND);

        assertEquals(new StockTransferCommand.Result(1, 2), result);
        assertEquals(60, from.getAmount(ITEM_1));
        assertEquals(Integer.MAX_VALUE, to.getAmount(ITEM_1));
        assertEquals(30, from.getAmount(ITEM_2));
        assertEquals(Integer.MAX_VALUE, to.getAmount(ITEM_2));
    }

    @Test
    void transferAllWithEmptySourceDoesNothing() {
        StockHolder from = holder();
        StockHolder to = holder();
        to.setAmount(ITEM_1, 3, IN);

        assertEquals(new StockTransferCommand.Result(0, 0), StockTransferCommand.transferAll(from, to, OUT, IN, REFUND));
        assertEquals(3, to.getAmount(ITEM_1));
    }

    @Test
    void transferAllRefundsAmountTheDestinationCouldNotAccept() {
        StockHolder from = holder();
        StockHolder realTo = holder();
        from.setAmount(ITEM_1, 100, OUT);

        // Simulates the destination stock growing concurrently after the capacity check: only 30 can be added.
        StockHolder to = new StockHolderWrapper() {
            @Override
            public @NotNull StockHolder delegate() {
                return realTo;
            }

            @Override
            public int increaseToMax(@NotNull BoxItem item, int increment, StockEvent.@NotNull Cause cause) {
                return realTo.increaseToMax(item, Math.min(increment, 30), cause);
            }
        };

        StockTransferCommand.Result result = StockTransferCommand.transferAll(from, to, OUT, IN, REFUND);

        assertEquals(new StockTransferCommand.Result(1, 1), result);
        assertEquals(70, from.getAmount(ITEM_1));
        assertEquals(30, realTo.getAmount(ITEM_1));
    }

    @Test
    void transferAllRefundsWhenIncreasingFails() {
        StockHolder from = holder();
        StockHolder realTo = holder();
        from.setAmount(ITEM_1, 100, OUT);

        StockHolder to = new StockHolderWrapper() {
            @Override
            public @NotNull StockHolder delegate() {
                return realTo;
            }

            @Override
            public int increaseToMax(@NotNull BoxItem item, int increment, StockEvent.@NotNull Cause cause) {
                throw new IllegalStateException("test");
            }
        };

        assertThrows(IllegalStateException.class, () -> StockTransferCommand.transferAll(from, to, OUT, IN, REFUND));
        assertEquals(100, from.getAmount(ITEM_1));
        assertEquals(0, realTo.getAmount(ITEM_1));
    }

    private static StockHolder holder() {
        return TestStockHolder.create(VoidStockEventCaller.INSTANCE, id -> ITEMS.get(id - 1));
    }
}
