package net.okocraft.box.feature.craft.util;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.craft.event.BoxCraftEvent;
import net.okocraft.box.feature.craft.event.stock.CraftCause;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ItemCrafter {

    public static boolean canCraft(@NotNull StockHolder stockHolder, @NotNull SelectedRecipe recipe, int times) {
        var ingredientMap = new HashMap<BoxItem, Integer>();

        for (var ingredient : recipe.ingredients()) {
            ingredientMap.put(
                    ingredient.item(),
                    ingredientMap.getOrDefault(ingredient.item(), 0) + ingredient.amount()
            );
        }

        for (var ingredient : ingredientMap.entrySet()) {
            var item = ingredient.getKey();

            int need = ingredient.getValue() * times;
            int current = stockHolder.getAmount(item);

            if (current < need) {
                return false;
            }
        }

        return true;
    }

    public static boolean craft(@NotNull Player crafter, @NotNull SelectedRecipe recipe, int times) {
        var stockHolder = PlayerSession.get(crafter).getStockHolder();

        if (!canCraft(stockHolder, recipe, times)) {
            return false;
        }

        var event = new BoxCraftEvent(crafter, recipe, times);
        BoxProvider.get().getEventBus().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        var cause = new CraftCause(crafter, recipe);

        Object2IntMap<BoxItem> ingredientMap = new Object2IntArrayMap<>(recipe.ingredients().size());
        Object2IntMap<BoxItem> craftRemainingItemMap = null;

        for (var ingredient : recipe.ingredients()) {
            int amount = ingredient.amount() * times;

            ingredientMap.mergeInt(ingredient.item(), amount, Integer::sum);

            var remainingItem = ingredient.item().getOriginal().getType().getCraftingRemainingItem();

            if (remainingItem != null) {
                var remainingBoxItem = BoxProvider.get().getItemManager().getBoxItem(remainingItem.name());

                if (remainingBoxItem.isPresent()) {
                    if (craftRemainingItemMap == null) {
                        craftRemainingItemMap = new Object2IntArrayMap<>(recipe.ingredients().size());
                    }
                    craftRemainingItemMap.mergeInt(remainingBoxItem.get(), amount, Integer::sum);
                }
            }
        }

        if (!stockHolder.decreaseIfPossible(ingredientMap, cause)) {
            return false;
        }

        if (craftRemainingItemMap != null) {
            for (var entry : craftRemainingItemMap.object2IntEntrySet()) {
                stockHolder.increase(entry.getKey(), entry.getIntValue(), cause);
            }
        }

        int resultAmount = recipe.amount() * times;
        int storeAmount = resultAmount;

        if (Distribution.toInventory(crafter)) {
            var result =
                    BoxProvider.get().getTaskFactory()
                            .supplyFromEntity(crafter, player -> InventoryTransaction.withdraw(player.getInventory(), recipe.result(), resultAmount))
                            .join();

            if (result.getType().isModified()) {
                storeAmount = resultAmount - result.getAmount();
            }
        }

        if (storeAmount != 0) {
            stockHolder.increase(recipe.result(), storeAmount, cause);
        }

        return true;
    }
}
