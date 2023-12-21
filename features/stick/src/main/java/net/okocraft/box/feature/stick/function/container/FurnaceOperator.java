package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.jetbrains.annotations.NotNull;

public final class FurnaceOperator {

    public static boolean process(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        if (context.operationType() == ContainerOperation.OperationType.DEPOSIT) {
            return takeResultItem(context);
        } else {
            return putIngredient(context) || putFuel(context);
        }
    }

    private static boolean takeResultItem(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        var result = context.inventory().getResult();

        if (result == null) {
            return false;
        }

        var boxItem = BoxAPI.api().getItemManager().getBoxItem(result);

        if (boxItem.isEmpty()) {
            return false;
        }

        var clickEvent = new InventoryClickEvent(context.view(), InventoryType.SlotType.RESULT, 2, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        boolean cancelled = !clickEvent.callEvent();

        if (cancelled) {
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
                () -> new InventoryClickEvent(context.view(), InventoryType.SlotType.CRAFTING, 0, ClickType.LEFT, InventoryAction.PLACE_ALL),
                context.inventory()::setSmelting,
                () -> new StickCauses.Furnace(context.player(), context.blockLocation(), StickCauses.Furnace.Type.PUT_INGREDIENT)
        );
    }

    private static boolean putFuel(@NotNull ContainerOperation.Context<FurnaceInventory> context) {
        return ItemPutter.putItem(
                context.player(),
                context.inventory().getFuel(),
                context.inventory()::isFuel,
                () -> new InventoryClickEvent(context.view(), InventoryType.SlotType.FUEL, 1, ClickType.LEFT, InventoryAction.PLACE_ALL),
                context.inventory()::setFuel,
                () -> new StickCauses.Furnace(context.player(), context.blockLocation(), StickCauses.Furnace.Type.PUT_FUEL)
        );
    }
}
