package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModeButton extends AbstractAutoStoreSettingButton {

    public ModeButton() {
        super(10);
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return new ItemStack(Material.AIR);
        }

        var icon = new ItemStack(setting.isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH);

        icon.editMeta(meta -> editIconMeta(session.getViewer(), setting, meta));

        return icon;
    }

    private void editIconMeta(@NotNull Player viewer, @NotNull AutoStoreSetting setting, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE, viewer));

        var lore =
                setting.isAllMode() ?
                        AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM :
                        AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL;

        target.lore(List.of(Component.empty(), TranslationUtil.render(lore, viewer), Component.empty()));
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        ClickResult result;
        setting.setAllMode(!setting.isAllMode());

        if (!setting.isEnabled()) {
            setting.setEnabled(true);
            result = ClickResult.UPDATE_ICONS;
        } else {
            result = ClickResult.UPDATE_BUTTON;
        }

        SoundBase.CLICK.play(session.getViewer());
        callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
