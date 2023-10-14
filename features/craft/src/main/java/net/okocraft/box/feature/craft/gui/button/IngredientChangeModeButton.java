package net.okocraft.box.feature.craft.gui.button;

import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IngredientChangeModeButton implements Button {

    private final int slot;

    public IngredientChangeModeButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var changePerIngredientMode = session.getData(CurrentRecipe.CHANGE_PER_INGREDIENT) != null;
        var icon = new ItemStack(changePerIngredientMode ? Material.FIREWORK_STAR : Material.FIRE_CHARGE);

        icon.editMeta(meta -> meta.displayName(
                TranslationUtil.render(
                        changePerIngredientMode ? Displays.EACH_INGREDIENT_CHANGE_MODE : Displays.BULK_INGREDIENT_CHANGE_MODE,
                        session.getViewer()
                )
        ));

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (session.removeData(CurrentRecipe.CHANGE_PER_INGREDIENT) == null) {
            session.putData(CurrentRecipe.CHANGE_PER_INGREDIENT, Boolean.TRUE);
        }

        var clicker = session.getViewer();
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);

        return ClickResult.UPDATE_BUTTON;
    }
}
