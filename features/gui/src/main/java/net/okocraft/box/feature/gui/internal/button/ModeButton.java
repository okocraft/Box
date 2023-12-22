package net.okocraft.box.feature.gui.internal.button;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
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
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var editor = ItemEditor.create().displayName(DISPLAY_NAME.create(session.getMessageSource()));
        var currentMode = session.getBoxItemClickMode();

        for (var mode : session.getAvailableClickModes()) {
            var style = currentMode == mode ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY;
            editor.loreLine(Component.text().append(Component.text(" > ")).append(mode.getDisplayName(session)).style(style).build());
        }

        return editor.createItem(currentMode.getIconMaterial());
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var modes = session.getAvailableClickModes();
        int nextIndex = modes.indexOf(session.getBoxItemClickMode()) + 1;

        if (modes.size() <= nextIndex) {
            nextIndex = 0;
        }

        var mode = modes.get(nextIndex);

        session.setBoxItemClickMode(mode);
        mode.onSelect(session);

        SoundBase.CLICK.play(session.getViewer());

        return ClickResult.UPDATE_ICONS;
    }
}
