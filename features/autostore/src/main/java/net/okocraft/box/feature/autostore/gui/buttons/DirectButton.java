package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DirectButton extends AbstractAutoStoreSettingButton {

    public DirectButton() {
        super(15);
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var item = new ItemStack(Material.HOPPER);
        item.editMeta(meta -> editIconMeta(session, meta));
        return item;
    }

    private void editIconMeta(@NotNull PlayerSession session, @NotNull ItemMeta target) {
        var viewer = session.getViewer();

        target.displayName(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_DIRECT, viewer));

        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting != null) {
            var lore = TranslationUtil.render(
                    AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_DIRECT_LORE.apply(!setting.isDirect()),
                    viewer
            );

            target.lore(List.of(Component.empty(), lore, Component.empty()));
        }
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        ClickResult result;

        setting.setDirect(!setting.isDirect());

        if (setting.isDirect() && !setting.isEnabled()) {
            setting.setEnabled(true);
            result = ClickResult.UPDATE_ICONS;
        } else {
            result = ClickResult.UPDATE_BUTTON;
        }

        var clicker = session.getViewer();
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
