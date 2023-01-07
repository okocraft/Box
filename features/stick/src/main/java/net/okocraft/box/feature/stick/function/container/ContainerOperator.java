package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class ContainerOperator {

    public static boolean process(@NotNull BoxPlayer boxPlayer, @NotNull ContainerOperation.OperationType type,
                                  @NotNull Inventory inventory, @NotNull Location containerLocation) {
        if (type == ContainerOperation.OperationType.DEPOSIT) {
            return depositItemsInInventory(boxPlayer, inventory, containerLocation);
        } else {
            return withdrawToInventory(boxPlayer, inventory, containerLocation);
        }
    }

    private static boolean depositItemsInInventory(@NotNull BoxPlayer player, @NotNull Inventory inventory, @NotNull Location containerLocation) {
        var resultList = InventoryTransaction.depositItemsInInventory(inventory);

        if (resultList.getType().isModified()) {
            var cause = new StickCauses.Container(player, containerLocation);
            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> player.getCurrentStockHolder().increase(result.getItem(), result.getAmount(), cause));
            SoundPlayer.playDepositSound(player.getPlayer());
            return true;
        } else {
            return false;
        }
    }

    private static boolean withdrawToInventory(@NotNull BoxPlayer player, @NotNull Inventory inventory, @NotNull Location containerLocation) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(player.getPlayer().getInventory().getItemInMainHand()).orElse(null);

        if (boxItem == null) {
            return false;
        }

        var stockHolder = player.getCurrentStockHolder();
        var result = InventoryTransaction.withdraw(inventory, boxItem, stockHolder.getAmount(boxItem));

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount(), new StickCauses.Container(player, containerLocation));
            SoundPlayer.playWithdrawalSound(player.getPlayer());
            return true;
        } else {
            return false;
        }
    }
}
