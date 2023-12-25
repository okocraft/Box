package net.okocraft.box.feature.craft.gui.button;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.util.IngredientRenderer;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_YELLOW;

public class RecipeSelectButton implements Button {

    private static final MiniMessageBase CLICK_TO_SHOW_DETAILS = MiniMessageBase.messageKey(DisplayKeys.CLICK_TO_SHOW_DETAILS);

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
        var editor = ItemEditor.create()
                .displayName(translatable(this.recipe.result().getOriginal()).decoration(ITALIC, State.FALSE).append(space()).append(text("#" + number, NO_DECORATION_YELLOW)))
                .loreEmptyLine();

        boolean simple = session.getData(SHOW_DETAILS) == null;
        IngredientRenderer.render(editor, session, this.recipe, 1, simple);

        return editor.loreEmptyLine()
                .loreLine(
                        text(" -> ", NO_DECORATION_GRAY)
                                .append(translatable(recipe.result().getOriginal(), recipe.result().getDisplayName().style()))
                                .append(space())
                                .append(text("x", NO_DECORATION_GRAY))
                                .append(text(recipe.amount(), NO_DECORATION_AQUA))
                )
                .loreEmptyLineIf(simple)
                .loreLineIf(simple, () -> CLICK_TO_SHOW_DETAILS.create(session.getMessageSource()))
                .createItem(this.recipe.result().getOriginal().getType());
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
