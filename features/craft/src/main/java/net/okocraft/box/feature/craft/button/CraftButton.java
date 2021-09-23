package net.okocraft.box.feature.craft.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import net.okocraft.box.feature.craft.util.IngredientRenderer;
import net.okocraft.box.feature.craft.util.ItemCrafter;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public class CraftButton implements RefreshableButton {

    private final Supplier<SelectedRecipe> recipeSupplier;

    private final Supplier<Integer> timesSupplier;
    private final Player crafter;
    private final int slot;
    private final AtomicBoolean craftedFlag;
    private boolean customTimes;

    public CraftButton(@NotNull Supplier<SelectedRecipe> recipeSupplier, int times, @NotNull Player crafter,
                       int slot, @NotNull AtomicBoolean craftedFlag) {
        this(recipeSupplier, () -> times, crafter, slot, craftedFlag);
        customTimes = false; // overwrite
    }

    public CraftButton(@NotNull Supplier<SelectedRecipe> recipeSupplier, @NotNull Supplier<Integer> timesSupplier, @NotNull Player crafter,
                       int slot, @NotNull AtomicBoolean craftedFlag) {
        this.recipeSupplier = recipeSupplier;
        this.timesSupplier = timesSupplier;
        this.crafter = crafter;
        this.slot = slot;
        this.craftedFlag = craftedFlag;
        this.customTimes = true;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        if (customTimes) {
            return canCraft() ? Material.GLOWSTONE_DUST : Material.GUNPOWDER;
        } else {
            return canCraft() ? Material.LIME_DYE : Material.GRAY_DYE;
        }
    }

    @Override
    public int getIconAmount() {
        var times = timesSupplier.get();
        return 64 < times ? 1 : times;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var times = timesSupplier.get();

        target.displayName(TranslationUtil.render(Displays.CRAFT_BUTTON_DISPLAY_NAME.apply(times), viewer));

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(viewer).getCurrentStockHolder();

        var lore = new ArrayList<Component>();
        var recipe = recipeSupplier.get();

        lore.add(Component.empty());
        IngredientRenderer.render(lore, recipe, viewer, times);

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

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        Sound sound;
        float pitch;

        int times = timesSupplier.get();
        var recipe = recipeSupplier.get();

        if (ItemCrafter.craft(clicker, recipe, times)) {
            sound = Sound.BLOCK_LEVER_CLICK;
            pitch = 1.0f;
        } else {
            sound = Sound.ENTITY_ENDERMAN_TELEPORT;
            pitch = 1.5f;
        }

        clicker.playSound(clicker.getLocation(), sound, 100f, pitch);
        craftedFlag.set(true);
    }

    private boolean canCraft() {
        return ItemCrafter.canCraft(
                BoxProvider.get().getBoxPlayerMap().get(crafter).getCurrentStockHolder(),
                recipeSupplier.get(),
                timesSupplier.get()
        );
    }
}
