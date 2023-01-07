package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.stick.event.stock.StickCause;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BrewerOperator {

    public static boolean process(@NotNull BoxPlayer boxPlayer, @NotNull ContainerOperation.OperationType type,
                                  @NotNull BrewerInventory inventory, @NotNull Location brewerLocation) {
        var mainHand = boxPlayer.getPlayer().getInventory().getItemInMainHand();

        if (type == ContainerOperation.OperationType.DEPOSIT) {
            return takeResultPotions(boxPlayer, inventory, brewerLocation);
        } else if (isPotion(mainHand.getType())) {
            return putPotions(boxPlayer, inventory, mainHand, brewerLocation);
        } else if (mainHand.getType() == Material.BLAZE_POWDER) {
            return putBlazePowder(boxPlayer, inventory, brewerLocation);
        }

        return false;
    }

    private static boolean takeResultPotions(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory, @NotNull Location brewerLocation) {
        boolean result = false;
        StickCause cause = null;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = inventory.getItem(i);

            if (potion == null) {
                continue;
            }

            var boxItem = BoxProvider.get().getItemManager().getBoxItem(potion);

            if (boxItem.isPresent()) {
                if (cause == null) { // Initialize here and cache it.
                    cause = new StickCauses.Brewer(player, brewerLocation, StickCauses.Brewer.Type.TAKE_POTION);
                }

                player.getCurrentStockHolder().increase(boxItem.get(), potion.getAmount(), cause);
                inventory.setItem(i, null);
                result = true;
            }
        }

        if (result) {
            SoundPlayer.playDepositSound(player.getPlayer());
        }

        return result;
    }

    private static boolean putPotions(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory,
                                      @NotNull ItemStack mainHand, @NotNull Location brewerLocation) {
        var optionalBoxItem = BoxProvider.get().getItemManager().getBoxItem(mainHand);

        if (optionalBoxItem.isEmpty()) {
            return false;
        }

        boolean result = false;
        var stockHolder = player.getCurrentStockHolder();
        var item = optionalBoxItem.get();
        StickCause cause = null;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = inventory.getItem(i);

            if (potion != null) { // if the slot is not empty, ignore it.
                continue;
            }

            if (0 < stockHolder.getAmount(item)) {
                if (cause == null) { // Initialize here and cache it.
                    cause = new StickCauses.Brewer(player, brewerLocation, StickCauses.Brewer.Type.PUT_POTION);
                }

                stockHolder.decrease(item, 1, cause);
                inventory.setItem(i, item.getClonedItem());
                result = true;
            }
        }

        if (result) {
            SoundPlayer.playWithdrawalSound(player.getPlayer());
        }

        return result;
    }

    private static boolean putBlazePowder(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory, @NotNull Location brewerLocation) {
        return ItemPutter.putItem(
                player,
                inventory.getFuel(),
                item -> item.getType() == Material.BLAZE_POWDER,
                inventory::setFuel,
                () -> new StickCauses.Brewer(player, brewerLocation, StickCauses.Brewer.Type.PUT_BLAZE_POWDER)
        );
    }

    private static boolean isPotion(@NotNull Material material) {
        // see BrewingStandMenu.PotionSlot#mayPlaceItem
        return material == Material.POTION || material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION || material == Material.GLASS_BOTTLE;
    }
}
