package net.okocraft.box.feature.craft.gui.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.gui.util.IngredientRenderer;
import net.okocraft.box.feature.craft.gui.util.ItemCrafter;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.ToIntFunction;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public class CraftButton implements Button {

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
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var viewer = session.getViewer();
        var stockHolder = session.getStockHolder();
        var recipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY).getSelectedRecipe();
        int times = this.timesFunction.applyAsInt(session);

        boolean canCraft = ItemCrafter.canCraft(session.getStockHolder(), recipe, times);
        Material iconMaterial;

        if (this.customTimes) {
            iconMaterial = canCraft ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        } else {
            iconMaterial = canCraft ? Material.LIME_DYE : Material.GRAY_DYE;
        }

        var icon = new ItemStack(iconMaterial);

        icon.editMeta(target -> {
            target.displayName(TranslationUtil.render(Displays.CRAFT_BUTTON_DISPLAY_NAME.apply(times), viewer));

            var lore = new ArrayList<Component>();

            lore.add(Component.empty());
            IngredientRenderer.render(lore, viewer, recipe, times, stockHolder);

            lore.add(Component.empty());
            lore.add(
                    text(" -> ", NO_DECORATION_GRAY)
                            .append(translatable(recipe.result().getOriginal(), recipe.result().getDisplayName().style()))
                            .append(space())
                            .append(text("x", NO_DECORATION_GRAY))
                            .append(text(recipe.amount() * times, NO_DECORATION_AQUA))
            );

            lore.add(Component.empty());

            var currentStock = Displays.CRAFT_BUTTON_CURRENT_STOCK.apply(stockHolder.getAmount(recipe.result()));
            lore.add(TranslationUtil.render(currentStock, viewer));

            target.lore(lore);
        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var result = ClickResult.waitingTask();

        ItemCrafter.craft(session, this.timesFunction.applyAsInt(session), result);

        return result;
    }
}
