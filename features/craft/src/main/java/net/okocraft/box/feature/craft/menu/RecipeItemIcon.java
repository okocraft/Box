package net.okocraft.box.feature.craft.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public record RecipeItemIcon(@NotNull CraftMenu.CurrentRecipe currentRecipe,
                             @NotNull Player viewer, int slot, int pos,
                             @NotNull AtomicBoolean changeSameIngredientFlag,
                             @NotNull AtomicBoolean updateFlag, @NotNull Menu currentMenu) implements RenderedButton {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        var ingredients = currentRecipe.getIngredients(pos);

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

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(viewer).getCurrentStockHolder();

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
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public void updateIcon(@NotNull Player viewer) {
    }

    @Override
    public void clickButton(@NotNull Player clicker, @NotNull ClickType clickType) {
        if (clickType.isShiftClick()) {
            var ingredients = currentRecipe.getIngredients(pos);

            if (ingredients == null) {
                return;
            }

            var item = ingredients.getSelected().item();
            var recipes = RecipeRegistry.getRecipes(item);

            if (recipes == null || recipes.getRecipeList().isEmpty()) {
                return;
            }

            Menu menu;

            if (recipes.getRecipeList().size() == 1) {
                menu = new CraftMenu(recipes.getRecipeList().get(0), currentMenu);
            } else {
                menu = new RecipeSelector(item, recipes, currentMenu);
            }

            MenuOpener.open(menu, clicker);
            return;
        }

        currentRecipe.nextRecipe(pos, changeSameIngredientFlag.get());
        updateFlag.set(true);
    }
}
