package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleButton extends AbstractAutoStoreSettingButton {

    public ToggleButton() {
        super(13);
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return new ItemStack(Material.AIR);
        }

        var icon = new ItemStack(setting.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL);

        icon.editMeta(meta -> editIconMeta(session.getViewer(), setting, meta));

        return icon;
    }

    private void editIconMeta(@NotNull Player viewer, @NotNull AutoStoreSetting setting, @NotNull ItemMeta target) {
        var displayName = AutoStoreMessage.ENABLED_OR_DISABLED.apply(setting.isEnabled()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        target.displayName(TranslationUtil.render(displayName, viewer));

        var lore = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_BUTTON.apply(!setting.isEnabled());

        target.lore(List.of(
                Component.empty(),
                TranslationUtil.render(lore, viewer),
                Component.empty()
        ));
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        setting.setEnabled(!setting.isEnabled());

        var clicker = session.getViewer();
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);

        callAutoStoreSettingChangeEvent(setting);

        return ClickResult.UPDATE_BUTTON;
    }
}
