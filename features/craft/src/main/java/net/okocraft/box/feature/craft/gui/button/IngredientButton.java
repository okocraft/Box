package net.okocraft.box.feature.craft.gui.button;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.gui.SelectableIngredients;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.menu.RecipeSelectorMenu;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record IngredientButton(int slot, int ingredientPos) implements Button {

    public static final TypedKey<Boolean> CHANGE_PER_INGREDIENT = TypedKey.of(Boolean.class, "change_per_ingredient");
    private static final MessageKey CLICK_TO_SHOW_RECIPES = MessageKey.key(DisplayKeys.INGREDIENT_BUTTON_CLICK_TO_SHOW_RECIPES);

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        CurrentRecipe currentRecipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);
        SelectableIngredients ingredients = currentRecipe.getIngredients(this.ingredientPos);

        if (ingredients == null) {
            return new ItemStack(Material.AIR);
        }

        ItemEditor editor = ItemEditor.create();

        if (ingredients.size() != 1) {
            for (BoxIngredientItem ingredient : ingredients.get()) {
                editor.loreLine(
                    Component.text()
                        .append(Component.text(" > "))
                        .append(Component.translatable(ingredient.item().getOriginal()))
                        .append(Component.space())
                        .append(Component.text("(" + session.getSourceStockHolder().getAmount(ingredient.item()) + ")"))
                        .style(ingredient == ingredients.getSelected() ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY)
                        .build()
                );
            }
        }

        if (RecipeRegistry.hasRecipe(ingredients.getSelected().item())) {
            editor.loreEmptyLineIf(ingredients.size() != 1)
                .loreLine(CLICK_TO_SHOW_RECIPES);
        }

        BoxIngredientItem selected = ingredients.getSelected();
        return editor.applyTo(session.getViewer(), selected.item().getOriginal().asQuantity(selected.amount()));
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        CurrentRecipe currentRecipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);

        if (clickType.isShiftClick()) {
            SelectableIngredients ingredients = currentRecipe.getIngredients(this.ingredientPos);

            if (ingredients == null) {
                return ClickResult.NO_UPDATE_NEEDED;
            }

            BoxItem item = ingredients.getSelected().item();
            RecipeHolder recipes = RecipeRegistry.getRecipes(item);

            if (recipes == null || recipes.getRecipeList().isEmpty()) {
                return ClickResult.NO_UPDATE_NEEDED;
            }

            Menu menu;

            if (recipes.getRecipeList().size() == 1) {
                menu = CraftMenu.prepare(recipes.getRecipeList().getFirst());
            } else {
                menu = new RecipeSelectorMenu(item, recipes);
            }

            return ClickResult.changeMenu(menu);
        } else {
            currentRecipe.nextRecipe(this.ingredientPos, session.getData(CHANGE_PER_INGREDIENT) == null);
            return ClickResult.UPDATE_ICONS;
        }
    }
}
