package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.buttons.BackButton;
import net.okocraft.box.feature.gui.api.menu.AbstractMenu;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoStoreSettingMenu extends AbstractMenu {

    private final AutoStoreSetting setting;

    private final AutoStoreModeButton modeButton;
    private final ToggleButton toggleButton;
    private final BulkEditingButton bulkEditingButton;
    private final BackButton backButton;

    AutoStoreSettingMenu(@NotNull AutoStoreSetting setting, @NotNull Menu backTo) {
        this.setting = setting;
        this.modeButton = new AutoStoreModeButton();
        this.toggleButton = new ToggleButton();
        this.bulkEditingButton = new BulkEditingButton();
        this.backButton = new BackButton(backTo, 22);
    }

    @Override
    public int getRows() {
        return 3;
    }

    @Override
    public @NotNull Component getTitle() {
        return AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public void updateMenu(@NotNull Player viewer) {
        var buttons = List.of(modeButton, toggleButton, bulkEditingButton, backButton);

        buttons.stream()
                .map(RenderedButton::create)
                .peek(button -> button.updateIcon(viewer))
                .forEach(this::addButton);
    }

    private class AutoStoreModeButton implements RefreshableButton {

        private AutoStoreModeButton() {
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return setting.isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH;
        }

        @Override
        public int getIconAmount() {
            return 1;
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
        public int getSlot() {
            return 11;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            setting.setAllMode(!setting.isAllMode());

            if (!setting.isEnabled()) {
                setting.setEnabled(true);
            }

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        }
    }

    private class ToggleButton implements RefreshableButton {

        private ToggleButton() {
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return setting.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
        }

        @Override
        public int getIconAmount() {
            return 1;
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            var displayName =
                    AutoStoreMessage.ENABLED_NAME.apply(setting.isEnabled())
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
        public int getSlot() {
            return 13;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            setting.setEnabled(!setting.isEnabled());
            clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        }
    }

    private class BulkEditingButton implements RefreshableButton {

        private Boolean recent = null;

        private BulkEditingButton() {
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return Material.TRIPWIRE_HOOK;
        }

        @Override
        public int getIconAmount() {
            return 1;
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            var displayName = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE;
            target.displayName(TranslationUtil.render(displayName, viewer));

            var lore = new ArrayList<Component>();

            lore.add(Component.empty());
            lore.add(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING.apply(recent == null || !recent));
            lore.add(Component.empty());

            if (recent != null) {
                lore.add(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RECENT.apply(recent));
                lore.add(Component.empty());
            }

            target.lore(TranslationUtil.render(lore, viewer));

            return target;
        }

        @Override
        public int getSlot() {
            return 15;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            var perItemSetting = setting.getPerItemModeSetting();
            Sound sound;

            if (recent == null || !recent) {
                perItemSetting.setEnabledItems(BoxProvider.get().getItemManager().getBoxItemSet());
                sound = Sound.BLOCK_WOODEN_DOOR_OPEN;
                recent = true;
            } else {
                perItemSetting.setEnabledItems(Collections.emptyList());
                sound = Sound.BLOCK_WOODEN_DOOR_CLOSE;
                recent = false;
            }

            if (!setting.isEnabled()) {
                setting.setEnabled(true);
            }

            if (setting.isAllMode()) {
                setting.setAllMode(false);
            }

            clicker.playSound(clicker.getLocation(), sound, 100f, 1.5f);
        }
    }
}
