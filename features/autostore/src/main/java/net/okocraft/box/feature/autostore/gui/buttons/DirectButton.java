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

public class DirectButton extends AbstractAutoStoreSettingButton {

    public DirectButton(@NotNull AutoStoreSetting setting) {
        super(15, setting);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.HOPPER;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_DIRECT, viewer));

        var lore = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_DIRECT_LORE.apply(!setting.isDirect());

        lore = TranslationUtil.render(lore, viewer);

        target.lore(List.of(Component.empty(), lore, Component.empty()));

        return target;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        setting.setDirect(!setting.isDirect());

        if (setting.isDirect() && !setting.isEnabled()) {
            setting.setEnabled(true);
        }

        SoundBase.CLICK.play(clicker);
        callAutoStoreSettingChangeEvent();
    }
}
