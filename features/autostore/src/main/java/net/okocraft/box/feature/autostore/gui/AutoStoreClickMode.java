package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.AdditionalButton;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;

public class AutoStoreClickMode implements BoxItemClickMode {

    private static final SoundBase ENABLE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON).pitch(1.5f).build();
    private static final SoundBase DISABLE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF).pitch(1.5f).build();

    @Override
    public @NotNull String getName() {
        return "autostore";
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.LEVER;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return AutoStoreMenuDisplays.AUTOSTORE_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        var player = context.clicker();
        var container = AutoStoreSettingContainer.INSTANCE;

        if (container.isLoaded(player)) {
            var playerSetting = container.get(player);
            var perItemSetting = playerSetting.getPerItemModeSetting();

            var enabled = !perItemSetting.isEnabled(context.item());

            perItemSetting.setEnabled(context.item(), enabled);

            playerSetting.setEnabled(true);
            playerSetting.setAllMode(false);

            (enabled ? ENABLE_SOUND : DISABLE_SOUND).play(player);
        }
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var container = AutoStoreSettingContainer.INSTANCE;

        if (!container.isLoaded(viewer)) {
            return;
        }

        var newLore = Optional.ofNullable(target.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        newLore.add(Component.empty());

        var enabled = container.get(viewer).getPerItemModeSetting().isEnabled(item);

        newLore.add(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_LORE.apply(enabled), viewer));

        newLore.add(Component.empty());

        target.lore(newLore);
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public boolean canUse(@NotNull Player viewer) {
        return viewer.hasPermission("box.autostore");
    }

    @Override
    public @NotNull AdditionalButton createAdditionalButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        if (AutoStoreSettingContainer.INSTANCE.isLoaded(viewer)) {
            return new AutoStoreSettingMenuButton(AutoStoreSettingContainer.INSTANCE.get(viewer), currentMenu);
        } else {
            return new AutoStoreSettingMenuButton(currentMenu);
        }
    }

    private static class AutoStoreSettingMenuButton extends AdditionalButton {

        private final AutoStoreSetting setting;
        private final Menu backTo;

        private AutoStoreSettingMenuButton(@NotNull AutoStoreSetting setting, @NotNull Menu backTo) {
            this.setting = setting;
            this.backTo = backTo;
        }

        private AutoStoreSettingMenuButton(@NotNull Menu backTo) {
            this.setting = null;
            this.backTo = backTo;
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
                    AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE.style(NO_DECORATION_GOLD),
                    viewer
            );

            target.displayName(displayName);

            return target;
        }

        @Override
        public final void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            if (setting != null) {
                MenuOpener.open(new AutoStoreSettingMenu(setting, backTo), clicker);
            }
        }
    }
}
