package net.okocraft.box.feature.craft.gui.button;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IngredientChangeModeButton implements Button {

    private static final MessageKey EACH_INGREDIENT_MODE = MessageKey.key(DisplayKeys.EACH_INGREDIENT_MODE);
    private static final MessageKey ALL_INGREDIENT_MODE = MessageKey.key(DisplayKeys.ALL_INGREDIENT_MODE);

    private final int slot;

    public IngredientChangeModeButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        boolean changePerIngredientMode = session.getData(IngredientButton.CHANGE_PER_INGREDIENT) != null;
        return ItemEditor.create()
            .displayName(changePerIngredientMode ? EACH_INGREDIENT_MODE : ALL_INGREDIENT_MODE)
            .createItem(session.getViewer(), changePerIngredientMode ? Material.FIREWORK_STAR : Material.FIRE_CHARGE);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (session.removeData(IngredientButton.CHANGE_PER_INGREDIENT) == null) {
            session.putData(IngredientButton.CHANGE_PER_INGREDIENT, Boolean.TRUE);
        }

        SoundBase.CLICK.play(session.getViewer());

        return ClickResult.UPDATE_BUTTON;
    }
}
