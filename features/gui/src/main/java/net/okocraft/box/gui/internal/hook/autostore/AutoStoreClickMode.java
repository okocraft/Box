package net.okocraft.box.gui.internal.hook.autostore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.AutoStoreSetting;
import net.okocraft.box.feature.autostore.model.SettingManager;
import net.okocraft.box.gui.api.menu.Menu;
import net.okocraft.box.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.gui.api.mode.SettingMenuButton;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import net.okocraft.box.gui.internal.lang.Styles;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AutoStoreClickMode implements BoxItemClickMode {

    private final SettingManager settingManager;

    AutoStoreClickMode(@NotNull SettingManager settingManager) {
        this.settingManager = settingManager;
    }

    @Override
    public @NotNull String getName() {
        return "autostore";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.AUTOSTORE_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        var player = context.clicker();

        var playerSetting = settingManager.get(player);
        var perItemSetting = playerSetting.getPerItemModeSetting();

        var enabled = !perItemSetting.isEnabled(context.item());

        perItemSetting.setEnabled(context.item(), enabled);

        playerSetting.setMode(perItemSetting);

        var sound = enabled ? Sound.BLOCK_WOODEN_BUTTON_CLICK_ON : Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF;
        player.playSound(player.getLocation(), sound, 100f, 1.5f);
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var result = new ArrayList<Component>();

        var original = target.lore();

        if (original != null) {
            result.addAll(original);
        }

        result.add(Component.empty());

        var enabled = settingManager.get(viewer).getPerItemModeSetting().isEnabled(item);

        result.add(TranslationUtil.render(Displays.AUTOSTORE_MODE_LORE.apply(enabled), viewer));

        result.add(Component.empty());

        target.lore(result);
    }

    @Override
    public boolean hasSettingMenu() {
        return true;
    }

    @Override
    public @NotNull SettingMenuButton createSettingMenuButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        return new AutoStoreSettingMenuButton(settingManager.get(viewer), currentMenu);
    }

    private static class AutoStoreSettingMenuButton extends SettingMenuButton {

        protected AutoStoreSettingMenuButton(@NotNull AutoStoreSetting setting, @NotNull Menu backTo) {
            super(() -> new AutoStoreSettingMenu(setting, backTo));
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return Material.SUNFLOWER;
        }

        @Override
        public int getIconAmount() {
            return 1;
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            var displayName = TranslationUtil.render(
                    Displays.AUTOSTORE_MODE_SETTING_MENU_TITLE.style(Styles.NO_STYLE).color(NamedTextColor.GOLD),
                    viewer
            );

            target.displayName(displayName);

            return target;
        }
    }
}
