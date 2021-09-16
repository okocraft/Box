package net.okocraft.box.gui.internal.button;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.gui.api.button.RefreshableButton;
import net.okocraft.box.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import net.okocraft.box.gui.internal.lang.Styles;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModeButton implements RefreshableButton {

    private final int slot;

    private int currentIndex = 0;
    private BoxItemClickMode currentMode = ClickModeRegistry.getModes().get(currentIndex);

    public ModeButton(int slot) {
        this.slot = slot;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.REDSTONE_TORCH;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Contract("_, _ -> param2")
    @Override
    public @NotNull ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.MODE_BUTTON, viewer));

        target.lore(TranslationUtil.render(createLore(), viewer));

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var modes = ClickModeRegistry.getModes();
        int temp = currentIndex + 1;

        if (modes.size() <= temp) {
            temp = 0;
        }

        currentIndex = temp;
        currentMode = modes.get(currentIndex);

        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
    }

    public @NotNull BoxItemClickMode getCurrentMode() {
        return currentMode;
    }

    private @NotNull List<Component> createLore() {
        var result = new ArrayList<Component>();

        var modes = ClickModeRegistry.getModes();

        for (int i = 0, limit = modes.size(); i < limit; i++) {
            var color = i == currentIndex ? NamedTextColor.AQUA : NamedTextColor.GRAY;

            result.add(
                    Component.text()
                            .append(Component.text(" > "))
                            .append(modes.get(i).getDisplayName())
                            .style(Styles.NO_STYLE)
                            .color(color)
                            .build()
            );
        }

        return result;
    }
}
