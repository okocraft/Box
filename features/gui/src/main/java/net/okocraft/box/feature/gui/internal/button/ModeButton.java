package net.okocraft.box.feature.gui.internal.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ModeButton implements Button {

    private final int slot;

    public ModeButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var currentMode = session.getBoxItemClickMode();
        var icon = new ItemStack(currentMode.getIconMaterial());

        icon.editMeta(meta -> {
            meta.displayName(TranslationUtil.render(Displays.MODE_BUTTON, session.getViewer()));

            var modes = session.getAvailableClickModes();
            var lore = new ArrayList<Component>(modes.size());

            for (var mode : modes) {
                var style = currentMode == mode ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY;

                lore.add(TranslationUtil.render(
                        Component.text()
                                .append(Component.text(" > "))
                                .append(mode.getDisplayName())
                                .style(style)
                                .build(),
                        session.getViewer()
                ));
            }

            meta.lore(lore);
        });

        return icon;
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

        var clicker = session.getViewer();
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);

        return ClickResult.UPDATE_ICONS;
    }
}
