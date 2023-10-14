package net.okocraft.box.feature.craft.gui.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.menu.RecipeSelectorMenu;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public record IngredientButton(int slot, int ingredientPos) implements Button {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var viewer = session.getViewer();
        var currentRecipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);

        var ingredients = currentRecipe.getIngredients(ingredientPos);

        if (ingredients == null) {
            return new ItemStack(Material.AIR);
        }

        var selected = ingredients.getSelected();
        var icon = selected.item().getClonedItem();

        icon.setAmount(selected.amount());

        var meta = icon.getItemMeta();

        if (meta == null) {
            return icon;
        }

        var lore = new ArrayList<Component>();

        var stockHolder = session.getStockHolder();

        if (ingredients.size() != 1) {
            for (var ingredient : ingredients.get()) {
                lore.add(
                        Component.text()
                                .append(Component.text(" > "))
                                .append(Component.translatable(ingredient.item().getOriginal()))
                                .append(Component.space())
                                .append(Component.text("(" + stockHolder.getAmount(ingredient.item()) + ")"))
                                .style(ingredient == ingredients.getSelected() ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY)
                                .build()
                );
            }
        }

        if (RecipeRegistry.hasRecipe(ingredients.getSelected().item())) {
            if (ingredients.size() != 1) {
                lore.add(Component.empty());
            }

            lore.add(TranslationUtil.render(Displays.RECIPE_BUTTON_SHIFT_CLICK_TO_SHOW_RECIPES, viewer));
        }

        meta.lore(lore);
        icon.setItemMeta(meta);

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var currentRecipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);

        if (clickType.isShiftClick()) {
            var ingredients = currentRecipe.getIngredients(ingredientPos);

            if (ingredients == null) {
                return ClickResult.NO_UPDATE_NEEDED;
            }

            var item = ingredients.getSelected().item();
            var recipes = RecipeRegistry.getRecipes(item);

            if (recipes == null || recipes.getRecipeList().isEmpty()) {
                return ClickResult.NO_UPDATE_NEEDED;
            }

            Menu menu;

            if (recipes.getRecipeList().size() == 1) {
                menu = CraftMenu.prepare(session, recipes.getRecipeList().get(0));
            } else {
                menu = new RecipeSelectorMenu(item, recipes);
            }

            return ClickResult.changeMenu(menu);
        } else {
            currentRecipe.nextRecipe(ingredientPos, session.getData(CurrentRecipe.CHANGE_PER_INGREDIENT) == null);
            return ClickResult.UPDATE_ICONS;
        }
    }
}
