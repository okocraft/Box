package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class FurnaceOperator {

    public static boolean process(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        if (context.operationType() == ContainerOperation.OperationType.DEPOSIT) {
            return takeResultItem(context);
        } else {
            return putIngredient(context) || putFuel(context);
        }
    }

    private static boolean takeResultItem(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        ItemStack result = context.inventory().getResult();

        if (result == null) {
            return false;
        }

        Optional<BoxItem> boxItem = BoxAPI.api().getItemManager().getBoxItem(result);

        if (boxItem.isEmpty()) {
            return false;
        }

        context.player().getCurrentStockHolder().increase(boxItem.get(), result.getAmount(), new StickCauses.Furnace(context.player(), context.blockLocation(), StickCauses.Furnace.Type.TAKE_RESULT_ITEM));
        context.inventory().setResult(null);
        SoundPlayer.playDepositSound(context.player().getPlayer());
        return true;
    }

    private static boolean putIngredient(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        return ItemPutter.putItem(
            context.player(),
            context.inventory().getSmelting(),
            context.inventory()::canSmelt,
            context.inventory()::setSmelting,
            () -> new StickCauses.Furnace(context.player(), context.blockLocation(), StickCauses.Furnace.Type.PUT_INGREDIENT)
        );
    }

    private static boolean putFuel(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        return ItemPutter.putItem(
            context.player(),
            context.inventory().getFuel(),
            context.inventory()::isFuel,
            context.inventory()::setFuel,
            () -> new StickCauses.Furnace(context.player(), context.blockLocation(), StickCauses.Furnace.Type.PUT_FUEL)
        );
    }
}
