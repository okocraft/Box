package net.okocraft.box.feature.gui.api.buttons;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record BackOrCloseButton(int slot) implements Button {

    private static final MiniMessageBase BACK_DISPLAY_NAME = MiniMessageBase.messageKey(DisplayKeys.BACK);
    private static final MiniMessageBase CLOSE_DISPLAY_NAME = MiniMessageBase.messageKey(DisplayKeys.CLOSE);

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ItemEditor.create()
                .displayName((session.hasPreviousMenu() ? BACK_DISPLAY_NAME : CLOSE_DISPLAY_NAME).create(session.getMessageSource()))
                .createItem(Material.OAK_DOOR);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (session.hasPreviousMenu()) {
            return ClickResult.changeMenu(session.backMenu());
        } else {
            return CloseButton.close(session);
        }
    }
}
