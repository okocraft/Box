package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.event.stock.GuiCauses;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public abstract class AbstractStorageMode implements BoxItemClickMode {

    protected final @NotNull ClickResult processDeposit(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var viewer = session.getViewer();

        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int amount = amountData != null ? amountData.getValue() : 1;

        var scheduler = BoxProvider.get().getScheduler();
        var result = ClickResult.waitingTask();

        scheduler.runEntityTask(viewer, () -> {
            var resultList =
                    StockHolderTransaction
                            .create(session.getStockHolder())
                            .deposit(item, amount)
                            .fromInventory(viewer.getInventory(), new GuiCauses.Deposit(viewer));

            finishTransaction(!resultList.isEmpty(), viewer, Sound.ENTITY_ITEM_PICKUP, 1.0f, result);
        });

        return result;
    }

    protected final @NotNull ClickResult processWithdraw(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var viewer = session.getViewer();

        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int limit = amountData != null ? amountData.getValue() : 1;

        var stockHolder = session.getStockHolder();
        var currentStock = stockHolder.getAmount(item);

        if (currentStock < 1) {
            viewer.playSound(viewer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            return ClickResult.NO_UPDATE_NEEDED;
        }

        var amount = Math.min(currentStock, limit);

        var scheduler = BoxProvider.get().getScheduler();
        var result = ClickResult.waitingTask();

        scheduler.runEntityTask(viewer, () -> {
            var withdrawn =
                    StockHolderTransaction
                            .create(session.getStockHolder())
                            .withdraw(item, amount)
                            .toInventory(viewer.getInventory(), new GuiCauses.Withdraw(viewer)).amount();

            finishTransaction(0 < withdrawn, viewer, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, result);
        });

        return result;
    }

    private static void finishTransaction(boolean isTransacted,
                                          @NotNull Player viewer, @NotNull Sound successSound, float pitch,
                                          @NotNull ClickResult.WaitingTask clickResult) {
        if (isTransacted) {
            viewer.playSound(viewer.getLocation(), successSound, 100f, pitch);
            clickResult.completeAsync(ClickResult.UPDATE_ICONS);
        } else {
            viewer.playSound(viewer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            clickResult.completeAsync(ClickResult.NO_UPDATE_NEEDED);
        }
    }

    protected static abstract class AbstractDepositAllButton implements Button {

        private final int slot;
        private final BiPredicate<PlayerSession, ClickType> canDepositAll;
        private final ClickResult cancelledResult;

        protected AbstractDepositAllButton(int slot, @NotNull BiPredicate<PlayerSession, ClickType> canDepositAll, @NotNull ClickResult cancelledResult) {
            this.slot = slot;
            this.canDepositAll = canDepositAll;
            this.cancelledResult = cancelledResult;
        }

        @Override
        public int getSlot() {
            return slot;
        }

        @Override
        public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            if (canDepositAll.test(session, clickType)) {
                var viewer = session.getViewer();
                var scheduler = BoxProvider.get().getScheduler();
                var result = ClickResult.waitingTask();

                scheduler.runEntityTask(viewer, () -> {
                    var resultList =
                            StockHolderTransaction
                                    .create(session.getStockHolder())
                                    .depositAll()
                                    .fromInventory(viewer.getInventory(), new GuiCauses.Deposit(viewer));

                    finishTransaction(!resultList.isEmpty(), viewer, Sound.BLOCK_NOTE_BLOCK_HARP, 2.0f, result);
                });

                return result;
            } else {
                return cancelledResult;
            }
        }
    }
}
