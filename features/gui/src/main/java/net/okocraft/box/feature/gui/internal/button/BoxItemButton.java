package net.okocraft.box.feature.gui.internal.button;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.ClickModeHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BoxItemButton implements Button {

    private final BoxItem item;
    private final int slot;

    public BoxItemButton(@NotNull BoxItem item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ClickModeHolder.getFromSession(session).getCurrentMode().createItemIcon(session, item);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        return ClickModeHolder.getFromSession(session).getCurrentMode().onClick(session, item, clickType);
    }
}
