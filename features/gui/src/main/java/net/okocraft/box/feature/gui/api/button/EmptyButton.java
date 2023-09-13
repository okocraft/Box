package net.okocraft.box.feature.gui.api.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

record EmptyButton(Material material, int slot) implements Button {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(material);

        icon.editMeta(meta -> {
            meta.displayName(Component.empty());
            meta.lore(Collections.emptyList());
        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession clicker, @NotNull ClickType clickType) {
        return ClickResult.NO_UPDATE_NEEDED;
    }
}
