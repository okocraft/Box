package net.okocraft.box.feature.craft.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

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
        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(crafter).getCurrentStockHolder();

        if (!canCraft(stockHolder, recipe, times)) {
            return false;
        }

        for (var ingredient : recipe.ingredients()) {
            stockHolder.decrease(ingredient.item(), ingredient.amount() * times);
        }

        int resultAmount = recipe.amount() * times;
        int storeAmount = resultAmount;

        if (Distribution.toInventory(crafter)) {
            var result = CompletableFuture.supplyAsync(
                    () -> InventoryTransaction.withdraw(
                            crafter.getInventory(),
                            recipe.result(),
                            resultAmount
                    ), BoxProvider.get().getExecutorProvider().getMainThread()
            ).join();

            if (result.getType().isModified()) {
                storeAmount = resultAmount - result.getAmount();
            }
        }

        if (storeAmount != 0) {
            stockHolder.increase(recipe.result(), storeAmount);
        }

        return true;
    }
}
