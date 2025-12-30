package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class ItemPutter {

    static boolean putItem(@NotNull BoxPlayer boxPlayer,
                           @Nullable ItemStack currentItem,
                           @NotNull Predicate<ItemStack> itemChecker,
                           @NotNull Consumer<ItemStack> itemSetter,
                           @NotNull Supplier<StockEvent.Cause> causeSupplier) {
        ItemStack mainHandItem = boxPlayer.getPlayer().getInventory().getItemInMainHand();

        if ((currentItem != null && !currentItem.isSimilar(mainHandItem)) || !itemChecker.test(mainHandItem)) {
            return false;
        }

        BoxItem boxItem = BoxAPI.api().getItemManager().getBoxItem(mainHandItem).orElse(null);

        if (boxItem == null) {
            return false;
        }

        int currentAmount = currentItem != null ? currentItem.getAmount() : 0;
        int maxStackSize = boxItem.getOriginal().getMaxStackSize();
        int fuelStock = boxPlayer.getCurrentStockHolder().getAmount(boxItem);
        int newAmount = fuelStock < maxStackSize - currentAmount ? fuelStock + currentAmount : maxStackSize; // This has the same **meaning** as Math.min(fuelStock + currentAmount, 64), but when fuelStock + currentAmount overflows, the way using Math.min causes a bug.
        int consumption = newAmount - currentAmount;

        if (consumption <= 0) {
            return false;
        }

        if (boxPlayer.getCurrentStockHolder().decreaseIfPossible(boxItem, consumption, causeSupplier.get()) != -1) {
            itemSetter.accept(boxItem.getOriginal().asQuantity(newAmount));
            SoundPlayer.playWithdrawalSound(boxPlayer.getPlayer());
        }

        return true;
    }
}
