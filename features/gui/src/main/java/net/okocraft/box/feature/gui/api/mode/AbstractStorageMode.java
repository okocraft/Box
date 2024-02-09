package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.event.stock.GuiCauses;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public abstract class AbstractStorageMode implements BoxItemClickMode {

    private static final SoundBase DEPOSIT_SOUND = SoundBase.builder().sound(Sound.ENTITY_ITEM_PICKUP).build();
    private static final SoundBase WITHDRAW_SOUND = SoundBase.builder().sound(Sound.BLOCK_STONE_BUTTON_CLICK_ON).build();

    protected final @NotNull ClickResult processDeposit(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var viewer = session.getViewer();

        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int amount = amountData != null ? amountData.getValue() : 1;

        var result = ClickResult.waitingTask();

        BoxAPI.api().getScheduler().runEntityTask(viewer, () -> {
            var resultList =
                    StockHolderTransaction
                            .create(session.getSourceStockHolder())
                            .deposit(item, amount)
                            .fromInventory(viewer.getInventory(), new GuiCauses.Deposit(viewer));

            finishTransaction(!resultList.isEmpty(), viewer, DEPOSIT_SOUND, result);
        });

        return result;
    }

    protected final @NotNull ClickResult processWithdraw(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var viewer = session.getViewer();

        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int limit = amountData != null ? amountData.getValue() : 1;

        var stockHolder = session.getSourceStockHolder();
        var currentStock = stockHolder.getAmount(item);

        if (currentStock < 1) {
            SoundBase.UNSUCCESSFUL.play(viewer);
            return ClickResult.NO_UPDATE_NEEDED;
        }

        int amount = Math.min(currentStock, limit);
        var result = ClickResult.waitingTask();

        BoxAPI.api().getScheduler().runEntityTask(viewer, () -> {
            var withdrawn =
                    StockHolderTransaction
                            .create(session.getSourceStockHolder())
                            .withdraw(item, amount)
                            .toInventory(viewer.getInventory(), new GuiCauses.Withdraw(viewer)).amount();

            finishTransaction(0 < withdrawn, viewer, WITHDRAW_SOUND, result);
        });

        return result;
    }

    private static void finishTransaction(boolean isTransacted,
                                          @NotNull Player viewer, @NotNull SoundBase successSound,
                                          @NotNull ClickResult.WaitingTask clickResult) {
        if (isTransacted) {
            successSound.play(viewer);
            clickResult.completeAsync(ClickResult.UPDATE_ICONS);
        } else {
            SoundBase.UNSUCCESSFUL.play(viewer);
            clickResult.completeAsync(ClickResult.NO_UPDATE_NEEDED);
        }
    }

    protected static abstract class AbstractDepositAllButton implements Button {

        private static final SoundBase DEPOSIT_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_NOTE_BLOCK_HARP).pitch(2.0f).build();

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
                var scheduler = BoxAPI.api().getScheduler();
                var result = ClickResult.waitingTask();

                scheduler.runEntityTask(viewer, () -> {
                    var resultList =
                            StockHolderTransaction
                                    .create(session.getSourceStockHolder())
                                    .depositAll()
                                    .fromInventory(viewer.getInventory(), new GuiCauses.Deposit(viewer));
                    finishTransaction(!resultList.isEmpty(), viewer, DEPOSIT_ALL_SOUND, result);
                });

                return result;
            } else {
                return cancelledResult;
            }
        }
    }
}
