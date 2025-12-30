package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.api.transaction.TransactionResult;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ContainerOperator {

    public static boolean process(@NotNull ContainerOperation.Context<Inventory> context) {
        if (context.operationType() == ContainerOperation.OperationType.DEPOSIT) {
            return depositItemsInInventory(context);
        } else {
            return withdrawToInventory(context);
        }
    }

    private static boolean depositItemsInInventory(@NotNull ContainerOperation.Context<Inventory> context) {
        List<TransactionResult> resultList =
            StockHolderTransaction.create(context.player().getCurrentStockHolder())
                .depositAll()
                .fromInventory(context.inventory(), new StickCauses.Container(context.player(), context.blockLocation()));

        if (!resultList.isEmpty()) {
            SoundPlayer.playDepositSound(context.player().getPlayer());
            return true;
        } else {
            return false;
        }
    }

    private static boolean withdrawToInventory(@NotNull ContainerOperation.Context<Inventory> context) {
        BoxItem boxItem = BoxAPI.api().getItemManager().getBoxItem(context.player().getPlayer().getInventory().getItemInMainHand()).orElse(null);

        if (boxItem == null) {
            return false;
        }

        StockHolder stockHolder = context.player().getCurrentStockHolder();
        TransactionResult result =
            StockHolderTransaction.create(stockHolder)
                .withdraw(boxItem, Integer.MAX_VALUE)
                .toInventory(context.inventory(), new StickCauses.Container(context.player(), context.blockLocation()));

        if (0 < result.amount()) {
            SoundPlayer.playWithdrawalSound(context.player().getPlayer());
            return true;
        } else {
            return false;
        }
    }
}
