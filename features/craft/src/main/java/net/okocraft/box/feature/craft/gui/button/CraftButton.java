package net.okocraft.box.feature.craft.gui.button;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.gui.util.IngredientRenderer;
import net.okocraft.box.feature.craft.gui.util.ItemCrafter;
import net.okocraft.box.feature.craft.lang.CraftPlaceholders;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public class CraftButton implements Button {

    private static final MessageKey.Arg1<Integer> DISPLAY_NAME = MessageKey.arg1(DisplayKeys.CRAFT_BUTTON, CraftPlaceholders.TIMES);
    private static final MessageKey.Arg1<Integer> CURRENT_STOCK = MessageKey.arg1(DisplayKeys.CRAFT_BUTTON_CURRENT_STOCK, Placeholders.CURRENT);

    private final int slot;
    private final ToIntFunction<PlayerSession> timesFunction;
    private final boolean customTimes;

    public CraftButton(int slot, int times) {
        this.slot = slot;
        this.timesFunction = ignored -> times;
        this.customTimes = false;
    }

    public CraftButton(int slot, @NotNull TypedKey<Amount> timesKey) {
        this.slot = slot;
        this.timesFunction = session -> {
            var data = session.getData(timesKey);
            return data != null ? data.getValue() : 1;
        };
        this.customTimes = true;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var recipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY).getSelectedRecipe();
        int times = this.timesFunction.applyAsInt(session);

        var editor = ItemEditor.create().displayName(DISPLAY_NAME.apply(times));

        IngredientRenderer.render(editor.loreEmptyLine(), session, recipe, times);

        boolean canCraft = ItemCrafter.canCraft(session.getSourceStockHolder(), recipe, times);

        return editor.loreEmptyLine()
            .loreLine(
                text(" -> ", NO_DECORATION_GRAY)
                    .append(translatable(recipe.result().getOriginal(), recipe.result().getDisplayName().style()))
                    .append(space())
                    .append(text("x", NO_DECORATION_GRAY))
                    .append(text(recipe.amount() * times, NO_DECORATION_AQUA))
            )
            .loreEmptyLine()
            .loreLine(CURRENT_STOCK.apply(session.getSourceStockHolder().getAmount(recipe.result())))
            .createItem(
                session.getViewer(),
                this.customTimes ?
                    canCraft ? Material.GLOWSTONE_DUST : Material.GUNPOWDER :
                    canCraft ? Material.LIME_DYE : Material.GRAY_DYE
            );
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var result = ClickResult.waitingTask();
        ItemCrafter.craft(session, this.timesFunction.applyAsInt(session), result);
        return result;
    }
}
