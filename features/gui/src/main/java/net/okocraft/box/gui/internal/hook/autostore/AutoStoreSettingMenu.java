package net.okocraft.box.gui.internal.hook.autostore;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.model.AutoStoreSetting;
import net.okocraft.box.gui.api.button.Button;
import net.okocraft.box.gui.api.button.RefreshableButton;
import net.okocraft.box.gui.api.menu.AbstractMenu;
import net.okocraft.box.gui.api.menu.Menu;
import net.okocraft.box.gui.api.menu.RenderedButton;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.button.BackButton;
import net.okocraft.box.gui.internal.lang.Displays;
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
import java.util.Optional;

public class AutoStoreSettingMenu extends AbstractMenu {

    private final AutoStoreSetting setting;

    private final AutoStoreModeButton modeButton;
    private final BulkEditingButton bulkEditingButton;
    private final BackButton backButton;

    private int loreIndex = -1;

    private boolean modeChanged = false;

    AutoStoreSettingMenu(@NotNull AutoStoreSetting setting, @NotNull Menu backTo) {
        this.setting = setting;
        this.modeButton = new AutoStoreModeButton();
        this.bulkEditingButton = new BulkEditingButton();
        this.backButton = new BackButton(backTo, 22);
    }

    @Override
    public int getRows() {
        return 3;
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.AUTOSTORE_MODE_SETTING_MENU_TITLE;
    }

    @Override
    public boolean shouldUpdate() {
        var current = modeChanged;
        modeChanged = false;
        return current;
    }

    @Override
    public void updateMenu(@NotNull Player viewer) {
        var buttons = List.of(modeButton, bulkEditingButton, backButton);

        buttons.stream()
                .map(RenderedButton::new)
                .peek(button -> button.updateIcon(viewer))
                .forEach(this::addButton);
    }

    private class AutoStoreModeButton implements RefreshableButton {

        private AutoStoreModeButton() {
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH;
        }

        @Override
        public int getIconAmount() {
            return 1;
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            target.displayName(TranslationUtil.render(Displays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE, viewer));

            var result = new ArrayList<Component>();

            Optional.ofNullable(target.lore()).ifPresent(result::addAll);

            var lore = isAllMode() ? Displays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM : Displays.AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL;
            lore = TranslationUtil.render(lore, viewer);

            if (loreIndex == -1 || result.size() <= loreIndex) {
                result.add(Component.empty());

                loreIndex = result.size();
                result.add(lore);

                result.add(Component.empty());
            } else {
                result.set(loreIndex, lore);
            }

            target.lore(result);

            return target;
        }

        @Override
        public int getSlot() {
            return 11;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            var changeTo = isAllMode() ? setting.getPerItemModeSetting() : setting.getAllModeSetting();
            setting.setMode(changeTo);

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        }

        private boolean isAllMode() {
            return setting.getCurrentMode() == setting.getAllModeSetting();
        }
    }

    private class BulkEditingButton implements Button {

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
            target.displayName(TranslationUtil.render(Displays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE, viewer));

            var lore = List.of(
                    Component.empty(),
                    Displays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_LEFT_CLICK,
                    Displays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RIGHT_CLICK,
                    Component.empty()
            );

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
            Sound sound = null;

            if (clickType.isLeftClick()) {
                perItemSetting.setEnabledItems(BoxProvider.get().getItemManager().getBoxItemSet());
                sound = Sound.BLOCK_WOODEN_DOOR_OPEN;
            }

            if (clickType.isRightClick()) {
                perItemSetting.setEnabledItems(Collections.emptyList());
                sound = Sound.BLOCK_WOODEN_DOOR_CLOSE;
            }

            if (setting.getCurrentMode() != perItemSetting) {
                setting.setMode(perItemSetting);
                modeChanged = true;
            }

            if (sound != null) {
                clicker.playSound(clicker.getLocation(), sound, 100f, 1.5f);
            }
        }
    }
}
