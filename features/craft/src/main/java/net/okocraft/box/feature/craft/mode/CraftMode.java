package net.okocraft.box.feature.craft.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.menu.RecipeSelectorMenu;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public class CraftMode implements BoxItemClickMode {

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CRAFTING_TABLE;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.CRAFT_MODE;
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var icon = item.getClonedItem();

        icon.editMeta(target -> {
            var newLore = Optional.ofNullable(target.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

            newLore.add(Component.empty());

            if (RecipeRegistry.hasRecipe(item)) {
                newLore.add(TranslationUtil.render(Displays.CLICK_TO_SHOW_RECIPES, session.getViewer()));
            } else {
                newLore.add(TranslationUtil.render(Displays.RECIPE_NOT_FOUND, session.getViewer()));
            }

            newLore.add(Component.empty());

            target.lore(newLore);
        });

        return icon;
    }

    @Override
    public void onSelect(@NotNull PlayerSession session) {
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        var viewer = session.getViewer();
        var recipes = RecipeRegistry.getRecipes(item);

        if (recipes == null) {
            SoundBase.UNSUCCESSFUL.play(viewer);
            return ClickResult.NO_UPDATE_NEEDED;
        }

        Menu menu;

        if (recipes.getRecipeList().size() == 1) {
            menu = CraftMenu.prepare(session,recipes.getRecipeList().get(0));
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
    public boolean canUse(@NotNull Player viewer, @NotNull BoxPlayer source) {
        return viewer.hasPermission("box.craft");
    }
}
