package net.okocraft.box.feature.craft.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.button.RecipeButton;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.buttons.BackButton;
import net.okocraft.box.feature.gui.api.buttons.CloseButton;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeSelector extends AbstractPaginatedMenu<BoxItemRecipe> {

    private final BoxItem item;
    private final RecipeHolder recipeHolder;
    private final Menu backTo;

    public RecipeSelector(@NotNull BoxItem item, @NotNull RecipeHolder recipeHolder, @Nullable Menu backTo) {
        super(recipeHolder.getRecipeList());
        this.item = item;
        this.recipeHolder = recipeHolder;
        this.backTo = backTo;
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.RECIPE_SELECTOR_TITLE.apply(item);
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItemRecipe instance, int slot) {
        var number = recipeHolder.getRecipeList().indexOf(instance) + 1;
        return new RecipeButton(instance, number, slot, this);
    }

    @Override
    protected void addAdditionalButtons(@NotNull Player viewer, @NotNull List<Button> buttons) {
        buttons.add(backTo != null ? new BackButton(backTo, 49) : new CloseButton());
    }
}
