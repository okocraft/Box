package net.okocraft.box.feature.craft.gui.util;

import io.papermc.paper.registry.keys.SoundEventKeys;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.InventoryUtil;
import net.okocraft.box.feature.craft.event.BoxCraftEvent;
import net.okocraft.box.feature.craft.event.stock.CraftCause;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemCrafter {

    private static final SoundBase CRAFT_SOUND = SoundBase.builder().sound(SoundEventKeys.BLOCK_LEVER_CLICK).build();
    public static final TypedKey<Boolean> PUT_CRAFTED_ITEMS_INTO_INVENTORY = TypedKey.of(Boolean.class, "put_crafted_items_into_inventory");

    public static boolean canCraft(@NotNull StockHolder stockHolder, @NotNull SelectedRecipe recipe, int times) {
        Map<BoxItem, Integer> ingredientMap = new HashMap<>();

        for (BoxIngredientItem ingredient : recipe.ingredients()) {
            ingredientMap.put(
                ingredient.item(),
                ingredientMap.getOrDefault(ingredient.item(), 0) + ingredient.amount()
            );
        }

        for (Map.Entry<BoxItem, Integer> ingredient : ingredientMap.entrySet()) {
            BoxItem item = ingredient.getKey();

            int need = ingredient.getValue() * times;
            int current = stockHolder.getAmount(item);

            if (current < need) {
                return false;
            }
        }

        return true;
    }

    public static void craft(@NotNull PlayerSession session, int times, @NotNull ClickResult.WaitingTask waitingTask) {
        Player crafter = session.getViewer();
        SelectedRecipe recipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY).getSelectedRecipe();
        StockHolder stockHolder = session.getSourceStockHolder();

        BoxCraftEvent event = new BoxCraftEvent(crafter, recipe, times);
        BoxAPI.api().getEventCallers().sync().call(event);

        if (event.isCancelled()) {
            SoundBase.UNSUCCESSFUL.play(crafter);
            waitingTask.complete(ClickResult.NO_UPDATE_NEEDED);
            return;
        }

        CraftCause cause = new CraftCause(crafter, recipe);

        Object2IntMap<BoxItem> ingredientMap = new Object2IntArrayMap<>(recipe.ingredients().size());
        Object2IntMap<BoxItem> craftRemainingItemMap = null;

        for (BoxIngredientItem ingredient : recipe.ingredients()) {
            int amount = ingredient.amount() * times;

            ingredientMap.mergeInt(ingredient.item(), amount, Integer::sum);

            Material remainingItem = ingredient.item().getOriginal().getType().getCraftingRemainingItem();

            if (remainingItem != null) {
                Optional<BoxItem> remainingBoxItem = BoxAPI.api().getItemManager().getBoxItem(remainingItem.name());

                if (remainingBoxItem.isPresent()) {
                    if (craftRemainingItemMap == null) {
                        craftRemainingItemMap = new Object2IntArrayMap<>(recipe.ingredients().size());
                    }
                    craftRemainingItemMap.mergeInt(remainingBoxItem.get(), amount, Integer::sum);
                }
            }
        }

        if (!stockHolder.decreaseIfPossible(ingredientMap, cause)) {
            SoundBase.UNSUCCESSFUL.play(crafter);
            waitingTask.complete(ClickResult.NO_UPDATE_NEEDED);
            return;
        }

        if (craftRemainingItemMap != null) {
            for (Object2IntMap.Entry<BoxItem> entry : craftRemainingItemMap.object2IntEntrySet()) {
                stockHolder.increase(entry.getKey(), entry.getIntValue(), cause);
            }
        }

        int resultAmount = recipe.amount() * times;

        if (session.getData(PUT_CRAFTED_ITEMS_INTO_INVENTORY) != null) {
            BoxAPI.api().getScheduler().runEntityTask(crafter, () -> {
                int remaining = InventoryUtil.putItems(crafter.getInventory(), recipe.result().getOriginal(), resultAmount);

                if (0 < remaining) {
                    stockHolder.increase(recipe.result(), remaining, cause);
                }

                CRAFT_SOUND.play(crafter);

                waitingTask.completeAsync(ClickResult.UPDATE_ICONS);
            });
        } else {
            waitingTask.complete(ClickResult.UPDATE_ICONS);
            stockHolder.increase(recipe.result(), resultAmount, cause);
            CRAFT_SOUND.play(crafter);
        }
    }
}
