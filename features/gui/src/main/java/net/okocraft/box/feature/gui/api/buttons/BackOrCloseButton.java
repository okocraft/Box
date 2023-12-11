package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record BackOrCloseButton(int slot) implements Button {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.OAK_DOOR);

        var display = session.hasPreviousMenu() ? Displays.BACK_BUTTON : Displays.CLOSE_BUTTON;
        icon.editMeta(meta -> meta.displayName(TranslationUtil.render(display, session.getViewer())));

        return icon;
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
