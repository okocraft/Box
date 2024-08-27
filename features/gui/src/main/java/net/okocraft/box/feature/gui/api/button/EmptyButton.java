package net.okocraft.box.feature.gui.api.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

record EmptyButton(Material material, int slot) implements Button {

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ItemEditor.create()
            .displayName(Component.empty())
            .clearLore()
            .createItem(this.material);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession clicker, @NotNull ClickType clickType) {
        return ClickResult.NO_UPDATE_NEEDED;
    }
}
