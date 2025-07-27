package net.okocraft.box.feature.gui.internal.button;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.ClickModeHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ModeButton(int slot) implements Button {

    private static final MiniMessageBase DISPLAY_NAME = MiniMessageBase.messageKey(DisplayKeys.MODE_CHANGE);

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var editor = ItemEditor.create().displayName(DISPLAY_NAME.create(session.getMessageSource()));
        var holder = ClickModeHolder.getFromSession(session);

        for (var mode : holder.getAvailableModes()) {
            var style = holder.getCurrentMode() == mode ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY;
            editor.loreLine(Component.text().append(Component.text(" > ")).append(mode.getDisplayName(session)).style(style).build());
        }

        return editor.createItem(holder.getCurrentMode().getIconMaterial());
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var holder = ClickModeHolder.getFromSession(session);
        int nextIndex = holder.getAvailableModes().indexOf(holder.getCurrentMode()) + 1;

        if (holder.getAvailableModes().size() <= nextIndex) {
            nextIndex = 0;
        }

        var mode = holder.getAvailableModes().get(nextIndex);

        holder.setCurrentMode(mode);
        ClickResult result = mode.onSelect(session);

        SoundBase.CLICK.play(session.getViewer());

        return result;
    }
}
