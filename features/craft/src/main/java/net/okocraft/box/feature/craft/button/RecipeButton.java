package net.okocraft.box.feature.craft.button;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.menu.CraftMenu;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.util.IngredientRenderer;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.api.buttons.MenuButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_STYLE;

public class RecipeButton extends MenuButton implements RefreshableButton {

    private final BoxItemRecipe recipe;
    private final int number;
    private final int slot;

    private boolean simple = true;

    public RecipeButton(@NotNull BoxItemRecipe recipe, int number,
                        int slot, @Nullable Menu backTo) {
        super(() -> new CraftMenu(recipe, backTo));
        this.recipe = recipe;
        this.number = number;
        this.slot = slot;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return recipe.result().getOriginal().getType();
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        if (clickType.isShiftClick()) {
            simple = !simple;
            return;
        }

        super.onClick(clicker, clickType);
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(
                translatable(recipe.result().getOriginal()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .append(Component.space())
                        .append(text("#" + number, NO_STYLE.color(YELLOW)))
        );

        var lore = new ArrayList<Component>();
        lore.add(Component.empty());

        IngredientRenderer.render(lore, recipe, viewer, 1, simple);

        lore.add(Component.empty());
        lore.add(
                text(" -> ", NO_STYLE.color(GRAY))
                        .append(translatable(recipe.result().getOriginal(), recipe.result().getDisplayName().style()))
                        .append(space())
                        .append(text("x", GRAY))
                        .append(text(recipe.amount(), AQUA))
        );

        if (simple) {
            lore.add(Component.empty());
            lore.add(TranslationUtil.render(Displays.RECIPE_BUTTON_CLICK_TO_SHOW_DETAILS, viewer));
        }

        target.lore(lore);

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
