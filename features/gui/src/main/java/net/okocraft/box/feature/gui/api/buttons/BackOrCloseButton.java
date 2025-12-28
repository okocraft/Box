package net.okocraft.box.feature.gui.api.buttons;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.MenuHistoryHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record BackOrCloseButton(int slot) implements Button {

    private static final MessageKey BACK_DISPLAY_NAME = MessageKey.key(DisplayKeys.BACK);
    private static final MessageKey CLOSE_DISPLAY_NAME = MessageKey.key(DisplayKeys.CLOSE);

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ItemEditor.create()
            .displayName(MenuHistoryHolder.getFromSession(session).hasPreviousMenu() ? BACK_DISPLAY_NAME : CLOSE_DISPLAY_NAME)
            .createItem(session.getViewer(), Material.OAK_DOOR);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (MenuHistoryHolder.getFromSession(session).hasPreviousMenu()) {
            return ClickResult.BACK_MENU;
        } else {
            return CloseButton.close(session);
        }
    }
}
