package net.okocraft.box.feature.craft.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.menu.CraftMenu;
import net.okocraft.box.feature.craft.menu.RecipeSelector;
import net.okocraft.box.feature.craft.model2.RecipeHolder;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.SettingMenuButton;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CraftMode implements BoxItemClickMode {

    private final Map<BoxItem, RecipeHolder> recipeMap;

    public CraftMode(@NotNull Map<BoxItem, RecipeHolder> recipeMap) {
        this.recipeMap = recipeMap;
    }

    @Override
    public @NotNull String getName() {
        return "craft";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.CRAFT_MODE;
    }

    @Override
    public void onClick(@NotNull Context context) {
        var clicker = context.clicker();
        var recipes = recipeMap.get(context.item());

        if (recipes == null) {
            clicker.playSound(clicker.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            return;
        }

        Menu menu;

        if (recipes.getRecipeList().size() == 1) {
            menu = new CraftMenu(recipes.getRecipeList().get(0), context.currentMenu());
        } else {
            menu = new RecipeSelector(context.item(), recipes, context.currentMenu());
        }

        var holder = new BoxInventoryHolder(menu);

        holder.initializeMenu(clicker);
        holder.applyContents();

        CompletableFuture.runAsync(
                () -> clicker.openInventory(holder.getInventory()),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();

        clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 2.0f);
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var result = new ArrayList<Component>();
        var original = target.lore();

        if (original != null) {
            result.addAll(original);
        }

        result.add(Component.empty());

        if (recipeMap.containsKey(item)) {
            result.add(TranslationUtil.render(Displays.CLICK_TO_SHOW_RECIPES, viewer));
        } else {
            result.add(TranslationUtil.render(Displays.RECIPE_NOT_FOUND, viewer));
        }
        result.add(Component.empty());

        target.lore(result);
    }

    @Override
    public boolean hasSettingMenu() {
        return false;
    }

    @Override
    public @NotNull SettingMenuButton createSettingMenuButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        throw new UnsupportedOperationException();
    }
}
