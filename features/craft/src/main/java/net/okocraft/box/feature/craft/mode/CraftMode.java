package net.okocraft.box.feature.craft.mode;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.menu.RecipeSelectorMenu;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftMode implements BoxItemClickMode {

    private final MessageKey displayName;
    private final MessageKey clickToShowRecipes;
    private final MessageKey noRecipe;

    public CraftMode(@NotNull DefaultMessageCollector collector) {
        this.displayName = MessageKey.key(collector.add("box.craft.mode.display-name", "Craft mode"));
        this.clickToShowRecipes = MessageKey.key(collector.add("box.craft.mode.click-to-show-recipes", "<gray>Click to show recipes"));
        this.noRecipe = MessageKey.key(collector.add("box.craft.mode.no-recipe", "<red>There is no recipe"));
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CRAFTING_TABLE;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull PlayerSession session) {
        return this.displayName.asComponent();
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        return ItemEditor.create()
            .copyLoreFrom(item.getOriginal())
            .loreEmptyLine()
            .loreLine(RecipeRegistry.hasRecipe(item) ? this.clickToShowRecipes : this.noRecipe)
            .loreEmptyLine()
            .applyTo(session.getViewer(), item.getClonedItem());
    }

    @Override
    public @NotNull ClickResult onSelect(@NotNull PlayerSession session) {
        return ClickResult.UPDATE_ICONS;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        Player viewer = session.getViewer();
        RecipeHolder recipes = RecipeRegistry.getRecipes(item);

        if (recipes == null) {
            SoundBase.UNSUCCESSFUL.play(viewer);
            return ClickResult.NO_UPDATE_NEEDED;
        }

        Menu menu;

        if (recipes.getRecipeList().size() == 1) {
            menu = CraftMenu.prepare(recipes.getRecipeList().getFirst());
        } else {
            menu = new RecipeSelectorMenu(item, recipes);
        }

        return ClickResult.changeMenu(menu);
    }

    @Override
    public boolean hasAdditionalButton() {
        return false;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        return session.getViewer().hasPermission("box.craft");
    }
}
