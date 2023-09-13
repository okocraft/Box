package net.okocraft.box.feature.craft.gui.button;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.gui.util.IngredientRenderer;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_YELLOW;

public class RecipeSelectButton implements Button {

    private static final TypedKey<Boolean> SHOW_DETAILS = TypedKey.of(Boolean.class, "recipe_show_details");

    private final int slot;
    private final BoxItemRecipe recipe;
    private final int number;

    public RecipeSelectButton(int slot, @NotNull BoxItemRecipe recipe, int number) {
        this.slot = slot;
        this.recipe = recipe;
        this.number = number;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(recipe.result().getOriginal().getType());

        var viewer = session.getViewer();

        icon.editMeta(target -> {
            target.displayName(
                    translatable(recipe.result().getOriginal()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .append(Component.space())
                            .append(text("#" + number, NO_DECORATION_YELLOW))
            );

            boolean simple = session.getData(SHOW_DETAILS) == null;

            var lore = new ArrayList<Component>();
            lore.add(Component.empty());

            IngredientRenderer.render(lore, viewer, recipe, 1, simple, session.getStockHolder());

            lore.add(Component.empty());
            lore.add(
                    text(" -> ", NO_DECORATION_GRAY)
                            .append(translatable(recipe.result().getOriginal(), recipe.result().getDisplayName().style()))
                            .append(space())
                            .append(text("x", NO_DECORATION_GRAY))
                            .append(text(recipe.amount(), NO_DECORATION_AQUA))
            );

            if (simple) {
                lore.add(Component.empty());
                lore.add(TranslationUtil.render(Displays.RECIPE_BUTTON_SHIFT_CLICK_TO_SHOW_DETAILS, viewer));
            }

            target.lore(lore);
        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (clickType.isShiftClick()) {
            if (session.removeData(SHOW_DETAILS) == null) {
                session.putData(SHOW_DETAILS, Boolean.TRUE);
            }

            return ClickResult.UPDATE_ICONS;
        } else {
            var menu = CraftMenu.prepare(session, recipe);
            return ClickResult.changeMenu(menu);
        }
    }
}
