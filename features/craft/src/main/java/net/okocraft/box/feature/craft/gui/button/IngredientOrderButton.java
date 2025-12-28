package net.okocraft.box.feature.craft.gui.button;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IngredientOrderButton implements Button {

    private static final MessageKey INGREDIENT_ORDER_NORMAL = MessageKey.key(DisplayKeys.INGREDIENT_ORDER_NORMAL);
    private static final MessageKey INGREDIENT_ORDER_STOCK_AMOUNT = MessageKey.key(DisplayKeys.INGREDIENT_ORDER_STOCK_AMOUNT);

    private final int slot;

    public IngredientOrderButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var order = session.getData(CraftMenu.INGREDIENT_ORDER_KEY);
        return order == CraftMenu.IngredientOrder.STOCK_AMOUNT ?
            ItemEditor.create().displayName(INGREDIENT_ORDER_STOCK_AMOUNT).createItem(session.getViewer(), Material.SOUL_TORCH) :
            ItemEditor.create().displayName(INGREDIENT_ORDER_NORMAL).createItem(session.getViewer(), Material.TORCH);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var recipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);
        var order = session.getData(CraftMenu.INGREDIENT_ORDER_KEY);

        if (order == CraftMenu.IngredientOrder.STOCK_AMOUNT) {
            session.putData(CraftMenu.INGREDIENT_ORDER_KEY, CraftMenu.IngredientOrder.NORMAL);
            recipe.sortIngredients(null);
        } else {
            session.putData(CraftMenu.INGREDIENT_ORDER_KEY, CraftMenu.IngredientOrder.STOCK_AMOUNT);
            recipe.sortIngredients(CraftMenu.IngredientOrder.STOCK_AMOUNT.createSorter(session));
        }

        recipe.selectFirstIngredients();

        SoundBase.CLICK.play(session.getViewer());

        return ClickResult.UPDATE_ICONS;
    }
}
