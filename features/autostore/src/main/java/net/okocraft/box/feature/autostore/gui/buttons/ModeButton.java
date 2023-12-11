package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModeButton extends AbstractAutoStoreSettingButton {

    public ModeButton(@NotNull AutoStoreSetting setting) {
        super(10, setting);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return setting.isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE, viewer));

        var lore =
                setting.isAllMode() ?
                        AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM :
                        AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL;

        lore = TranslationUtil.render(lore, viewer);

        target.lore(List.of(Component.empty(), lore, Component.empty()));

        return target;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        setting.setAllMode(!setting.isAllMode());

        if (!setting.isEnabled()) {
            setting.setEnabled(true);
        }

        SoundBase.CLICK.play(clicker);
        callAutoStoreSettingChangeEvent();
    }
}
