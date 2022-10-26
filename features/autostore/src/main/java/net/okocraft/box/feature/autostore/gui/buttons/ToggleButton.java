package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToggleButton extends AbstractAutoStoreSettingButton {

    public ToggleButton(@NotNull AutoStoreSetting setting) {
        super(13, setting);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return setting.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var displayName =
                AutoStoreMessage.ENABLED_OR_DISABLED.apply(setting.isEnabled())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        target.displayName(TranslationUtil.render(displayName, viewer));

        var lore = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TOGGLE_BUTTON.apply(!setting.isEnabled());

        target.lore(List.of(
                Component.empty(),
                TranslationUtil.render(lore, viewer),
                Component.empty()
        ));

        return target;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        setting.setEnabled(!setting.isEnabled());
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        callAutoStoreSettingChangeEvent();
    }
}
