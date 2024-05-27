package net.okocraft.box.feature.craft.gui.button;

import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ResultButton(int slot) implements Button {

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return session.getDataOrThrow(CurrentRecipe.DATA_KEY).getResultPreview();
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        return ClickResult.NO_UPDATE_NEEDED;
    }
}
