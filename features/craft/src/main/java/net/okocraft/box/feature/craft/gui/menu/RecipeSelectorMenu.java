package net.okocraft.box.feature.craft.gui.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.gui.button.RecipeSelectButton;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeSelectorMenu extends AbstractPaginatedMenu<BoxItemRecipe> {

    private static final BackOrCloseButton BACK_OR_CLOSE_BUTTON = new BackOrCloseButton(49);

    private final BoxItem item;
    private final RecipeHolder recipeHolder;

    public RecipeSelectorMenu(@NotNull BoxItem item, @NotNull RecipeHolder recipeHolder) {
        super(6, recipeHolder.getRecipeList());
        this.item = item;
        this.recipeHolder = recipeHolder;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return Displays.RECIPE_SELECTOR_TITLE.apply(item);
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItemRecipe instance, int slot) {
        return new RecipeSelectButton(slot, instance, recipeHolder.getRecipeList().indexOf(instance) + 1);
    }

    @Override
    protected void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons) {
        buttons.add(BACK_OR_CLOSE_BUTTON);
    }
}
