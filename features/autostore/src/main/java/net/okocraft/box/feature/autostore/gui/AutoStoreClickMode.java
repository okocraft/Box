package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.AutoStoreSetting;
import net.okocraft.box.feature.autostore.model.SettingManager;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.SettingMenuButton;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_STYLE;

@SuppressWarnings("ClassCanBeRecord")
public class AutoStoreClickMode implements BoxItemClickMode {

    private final SettingManager settingManager;

    public AutoStoreClickMode(@NotNull SettingManager settingManager) {
        this.settingManager = settingManager;
    }

    @Override
    public @NotNull String getName() {
        return "autostore";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return AutoStoreMenuDisplays.AUTOSTORE_MODE_DISPLAY_NAME;
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
        var newLore = Optional.ofNullable(target.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        newLore.add(Component.empty());

        var enabled = settingManager.get(viewer).getPerItemModeSetting().isEnabled(item);

        newLore.add(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_LORE.apply(enabled), viewer));

        newLore.add(Component.empty());

        target.lore(newLore);
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
                    AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE.style(NO_STYLE.color(GOLD)),
                    viewer
            );

            target.displayName(displayName);

            return target;
        }
    }
}
