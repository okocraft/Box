package net.okocraft.box.feature.craft.button;

import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class IngredientChangeModeButton implements RefreshableButton {

    private final AtomicBoolean bulkIngredientChange;

    public IngredientChangeModeButton(@NotNull AtomicBoolean bulkIngredientChange) {
        this.bulkIngredientChange = bulkIngredientChange;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return bulkIngredientChange.get() ? Material.FIRE_CHARGE : Material.FIREWORK_STAR;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(
                TranslationUtil.render(
                        bulkIngredientChange.get() ?
                                Displays.BULK_INGREDIENT_CHANGE_MODE : Displays.EACH_INGREDIENT_CHANGE_MODE,
                        viewer
                )
        );

        return target;
    }

    @Override
    public int getSlot() {
        return 18;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        bulkIngredientChange.set(!bulkIngredientChange.get());
        SoundBase.CLICK.play(clicker);
    }
}
