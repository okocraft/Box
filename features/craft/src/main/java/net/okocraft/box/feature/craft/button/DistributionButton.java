package net.okocraft.box.feature.craft.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.util.Distribution;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DistributionButton implements RefreshableButton {

    private final Player player;

    public DistributionButton(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Distribution.toInventory(player) ? Material.PLAYER_HEAD : Material.CHEST;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.DISTRIBUTION_BUTTON_DISPLAY_NAME, viewer));

        boolean current = Distribution.toInventory(player);

        target.lore(List.of(
                Component.empty(),
                TranslationUtil.render(
                        Displays.DISTRIBUTION_CURRENT.apply(current), viewer
                ),
                Component.empty(),
                TranslationUtil.render(
                        Displays.DISTRIBUTION_CLICK_TO_CHANGE.apply(current), viewer
                ),
                Component.empty()
        ));

        return target;
    }

    @Override
    public int getSlot() {
        return 53;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        Distribution.toggle(clicker);
        SoundBase.CLICK.play(clicker);
    }
}
