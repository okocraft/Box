package net.okocraft.box.feature.craft.util;

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
import java.util.Optional;

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

        for (var ingredient : recipe.ingredients()) {
            int amount = ingredient.amount() * times;

            stockHolder.decrease(ingredient.item(), amount, cause);

            Optional.ofNullable(ingredient.item().getOriginal().getType().getCraftingRemainingItem())
                    .map(Enum::name)
                    .flatMap(BoxProvider.get().getItemManager()::getBoxItem)
                    .ifPresent(boxItem -> stockHolder.increase(boxItem, amount, cause));
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
