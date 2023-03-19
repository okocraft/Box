package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class ContainerOperator {

    public static boolean process(@NotNull ContainerOperation.Context<Inventory> context) {
        if (context.operationType() == ContainerOperation.OperationType.DEPOSIT) {
            return depositItemsInInventory(context);
        } else {
            return withdrawToInventory(context);
        }
    }

    private static boolean depositItemsInInventory(@NotNull ContainerOperation.Context<Inventory> context) {
        var resultList = InventoryTransaction.depositItemsInTopInventory(context.view());

        if (resultList.getType().isModified()) {
            var cause = new StickCauses.Container(context.player(), context.blockLocation());
            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> context.player().getCurrentStockHolder().increase(result.getItem(), result.getAmount(), cause));
            SoundPlayer.playDepositSound(context.player().getPlayer());
            return true;
        } else {
            return false;
        }
    }

    private static boolean withdrawToInventory(@NotNull ContainerOperation.Context<Inventory> context) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(context.player().getPlayer().getInventory().getItemInMainHand()).orElse(null);

        if (boxItem == null) {
            return false;
        }

        var stockHolder = context.player().getCurrentStockHolder();
        var result = InventoryTransaction.withdraw(context.view(), boxItem, stockHolder.getAmount(boxItem));

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount(), new StickCauses.Container(context.player(), context.blockLocation()));
            SoundPlayer.playWithdrawalSound(context.player().getPlayer());
            return true;
        } else {
            return false;
        }
    }
}
