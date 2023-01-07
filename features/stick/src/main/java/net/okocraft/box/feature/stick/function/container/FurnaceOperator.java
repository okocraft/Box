package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.Location;
import org.bukkit.inventory.FurnaceInventory;
import org.jetbrains.annotations.NotNull;

public final class FurnaceOperator {

    public static boolean process(@NotNull BoxPlayer boxPlayer, @NotNull ContainerOperation.OperationType type,
                                  @NotNull FurnaceInventory inventory, @NotNull Location furnaceLocation) {
        if (type == ContainerOperation.OperationType.DEPOSIT) {
            return takeResultItem(boxPlayer, inventory, furnaceLocation);
        } else {
            return putIngredient(boxPlayer, inventory, furnaceLocation) || putFuel(boxPlayer, inventory, furnaceLocation);
        }
    }

    private static boolean takeResultItem(@NotNull BoxPlayer boxPlayer, @NotNull FurnaceInventory inventory, @NotNull Location furnaceLocation) {
        var result = inventory.getResult();

        if (result == null) {
            return false;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(result);

        if (boxItem.isPresent()) {
            boxPlayer.getCurrentStockHolder().increase(boxItem.get(), result.getAmount(), new StickCauses.Furnace(boxPlayer, furnaceLocation, StickCauses.Furnace.Type.TAKE_RESULT_ITEM));
            inventory.setResult(null);
            SoundPlayer.playDepositSound(boxPlayer.getPlayer());
            return true;
        } else {
            return false;
        }
    }

    private static boolean putIngredient(@NotNull BoxPlayer player, @NotNull FurnaceInventory inventory, @NotNull Location furnaceLocation) {
        return ItemPutter.putItem(
                player,
                inventory.getSmelting(),
                inventory::canSmelt,
                inventory::setSmelting,
                () -> new StickCauses.Furnace(player, furnaceLocation, StickCauses.Furnace.Type.PUT_INGREDIENT)
        );
    }

    private static boolean putFuel(@NotNull BoxPlayer player, @NotNull FurnaceInventory inventory, @NotNull Location furnaceLocation) {
        return ItemPutter.putItem(
                player,
                inventory.getFuel(),
                inventory::isFuel,
                inventory::setFuel,
                () -> new StickCauses.Furnace(player, furnaceLocation, StickCauses.Furnace.Type.PUT_FUEL)
        );
    }
}
