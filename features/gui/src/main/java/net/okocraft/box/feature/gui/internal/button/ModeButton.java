package net.okocraft.box.feature.gui.internal.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModeButton implements RefreshableButton {

    private final int slot;
    private final AtomicBoolean updateFlag;

    private Material iconMaterial = Material.REDSTONE_TORCH; // temp

    public ModeButton(int slot, @NotNull AtomicBoolean updateFlag) {
        this.slot = slot;
        this.updateFlag = updateFlag;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return iconMaterial;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Contract("_, _ -> param2")
    @Override
    public @NotNull ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.MODE_BUTTON, viewer));

        target.lore(TranslationUtil.render(createLore(viewer), viewer));

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var session = PlayerSession.get(clicker);

        var modes = session.getAvailableClickModes();
        int nextIndex = modes.indexOf(session.getBoxItemClickMode()) + 1;

        if (modes.size() <= nextIndex) {
            nextIndex = 0;
        }

        session.setBoxItemClickMode(modes.get(nextIndex));

        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);

        updateFlag.set(true);
    }

    private @NotNull List<Component> createLore(@NotNull Player viewer) {
        var result = new ArrayList<Component>();

        var session = PlayerSession.get(viewer);

        var modes = session.getAvailableClickModes();
        var currentMode = session.getBoxItemClickMode();

        for (var mode : modes) {
            var style = currentMode == mode ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY;

            result.add(
                    Component.text()
                            .append(Component.text(" > "))
                            .append(mode.getDisplayName())
                            .style(style)
                            .build()
            );
        }

        return result;
    }

    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }
}
