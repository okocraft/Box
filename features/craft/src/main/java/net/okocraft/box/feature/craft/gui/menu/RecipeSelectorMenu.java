package net.okocraft.box.feature.craft.gui.menu;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.gui.button.RecipeSelectButton;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.api.menu.paginate.PaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeSelectorMenu extends AbstractPaginatedMenu<BoxItemRecipe> {

    private static final BackOrCloseButton BACK_OR_CLOSE_BUTTON = new BackOrCloseButton(49);
    private static final MessageKey.Arg1<BoxItem> TITLE = MessageKey.arg1(DisplayKeys.RECIPE_SELECTOR_TITLE, Placeholders.ITEM);

    private final BoxItem item;
    private final RecipeHolder recipeHolder;

    public RecipeSelectorMenu(@NotNull BoxItem item, @NotNull RecipeHolder recipeHolder) {
        super(6, recipeHolder.getRecipeList(), PaginatedMenu.createCurrentPageKey("recipe_selector:" + item.getPlainName()));
        this.item = item;
        this.recipeHolder = recipeHolder;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(this.item).asComponent();
    }

    @Override
    protected @NotNull Button createButton(@NotNull BoxItemRecipe instance, int slot) {
        return new RecipeSelectButton(slot, instance, this.recipeHolder.getRecipeList().indexOf(instance) + 1);
    }

    @Override
    protected void addAdditionalButtons(@NotNull PlayerSession session, @NotNull List<Button> buttons) {
        buttons.add(BACK_OR_CLOSE_BUTTON);
    }
}
