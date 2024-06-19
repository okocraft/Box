package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.stick.event.stock.StickCause;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.Material;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BrewerOperator {

    public static boolean process(@NotNull ContainerOperation.Context<BrewerInventory> context) {
        if (context.operationType() == ContainerOperation.OperationType.DEPOSIT) {
            return takeResultPotions(context);
        }

        var mainHand = context.player().getPlayer().getInventory().getItemInMainHand();

        if (isPotion(mainHand.getType())) {
            return putPotions(context, mainHand);
        } else if (mainHand.getType() == Material.BLAZE_POWDER) {
            return putBlazePowder(context);
        }

        return false;
    }

    private static boolean takeResultPotions(@NotNull ContainerOperation.Context<BrewerInventory> context) {
        boolean result = false;
        StickCause cause = null;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = context.inventory().getItem(i);

            if (potion == null) {
                continue;
            }

            var boxItem = BoxAPI.api().getItemManager().getBoxItem(potion);

            if (boxItem.isEmpty()) {
                continue;
            }

            if (cause == null) { // Initialize here and cache it.
                cause = new StickCauses.Brewer(context.player(), context.blockLocation(), StickCauses.Brewer.Type.TAKE_POTION);
            }

            context.player().getCurrentStockHolder().increase(boxItem.get(), potion.getAmount(), cause);
            context.inventory().setItem(i, null);
            result = true;
        }

        if (result) {
            SoundPlayer.playDepositSound(context.player().getPlayer());
        }

        return result;
    }

    private static boolean putPotions(@NotNull ContainerOperation.Context<BrewerInventory> context, @NotNull ItemStack mainHand) {
        var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(mainHand);

        if (optionalBoxItem.isEmpty()) {
            return false;
        }

        boolean result = false;
        var stockHolder = context.player().getCurrentStockHolder();
        var item = optionalBoxItem.get();
        StickCause cause = null;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = context.inventory().getItem(i);

            if (potion != null) { // if the slot is not empty, ignore it.
                continue;
            }

            if (cause == null) { // Initialize here and cache it.
                cause = new StickCauses.Brewer(context.player(), context.blockLocation(), StickCauses.Brewer.Type.PUT_POTION);
            }

            if (0 < stockHolder.getAmount(item)) {
                if (stockHolder.decreaseIfPossible(item, 1, cause) != -1) {
                    context.inventory().setItem(i, item.getClonedItem());
                    result = true;
                }
            }
        }

        if (result) {
            SoundPlayer.playWithdrawalSound(context.player().getPlayer());
        }

        return result;
    }

    private static boolean putBlazePowder(@NotNull ContainerOperation.Context<BrewerInventory> context) {
        return ItemPutter.putItem(
                context.player(),
                context.inventory().getFuel(),
                item -> item.getType() == Material.BLAZE_POWDER,
                context.inventory()::setFuel,
                () -> new StickCauses.Brewer(context.player(), context.blockLocation(), StickCauses.Brewer.Type.PUT_BLAZE_POWDER)
        );
    }

    private static boolean isPotion(@NotNull Material material) {
        // see BrewingStandMenu.PotionSlot#mayPlaceItem
        return material == Material.POTION || material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION || material == Material.GLASS_BOTTLE;
    }
}
