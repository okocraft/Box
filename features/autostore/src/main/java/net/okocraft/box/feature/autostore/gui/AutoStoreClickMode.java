package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.autostore.gui.buttons.BulkEditingButton;
import net.okocraft.box.feature.autostore.gui.buttons.DirectButton;
import net.okocraft.box.feature.autostore.gui.buttons.ModeButton;
import net.okocraft.box.feature.autostore.gui.buttons.ToggleButton;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;

public class AutoStoreClickMode implements BoxItemClickMode {

    private static final AutoStoreSettingMenu SETTING_MENU = new AutoStoreSettingMenu();

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.LEVER;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return AutoStoreMenuDisplays.AUTOSTORE_MODE_DISPLAY_NAME;
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var icon = item.getClonedItem();

        var newLore = Optional.ofNullable(icon.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        newLore.add(Component.empty());

        var viewer = session.getViewer();

        var enabled = AutoStoreSettingContainer.INSTANCE.get(viewer).getPerItemModeSetting().isEnabled(item);

        newLore.add(TranslationUtil.render(AutoStoreMenuDisplays.AUTOSTORE_MODE_LORE.apply(enabled), viewer));

        newLore.add(Component.empty());

        icon.lore(newLore);

        return icon;
    }

    @Override
    public void onSelect(@NotNull PlayerSession session) {
        var source = session.getSource();
        var container = AutoStoreSettingContainer.INSTANCE;

        if (container.isLoaded(source.getPlayer())) {
            session.putData(AutoStoreSettingKey.KEY, container.get(source.getPlayer()));
        } else if (container.isLoaded(session.getViewer())) {
            session.putData(AutoStoreSettingKey.KEY, container.get(session.getViewer()));
        }
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        var player = session.getViewer();

        var playerSetting = session.getData(AutoStoreSettingKey.KEY);

        if (playerSetting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        var perItemSetting = playerSetting.getPerItemModeSetting();

        var enabled = !perItemSetting.isEnabled(item);

        perItemSetting.setEnabled(item, enabled);

        playerSetting.setEnabled(true);
        playerSetting.setAllMode(false);

        var sound = enabled ? Sound.BLOCK_WOODEN_BUTTON_CLICK_ON : Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF;
        player.playSound(player.getLocation(), sound, 100f, 1.5f);

        return ClickResult.UPDATE_BUTTON;
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public boolean canUse(@NotNull Player viewer, @NotNull BoxPlayer source) {
        if (viewer.hasPermission("box.autostore")) {
            var container = AutoStoreSettingContainer.INSTANCE;
            return container.isLoaded(source.getPlayer()) || container.isLoaded(viewer);
        } else {
            return false;
        }
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new AutoStoreSettingMenuButton(slot);
    }

    private record AutoStoreSettingMenuButton(int slot) implements Button {

        @Override
        public int getSlot() {
            return slot;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            var item = new ItemStack(Material.SUNFLOWER);

            item.editMeta(meta -> meta.displayName(TranslationUtil.render(
                    AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE.style(NO_DECORATION_GOLD),
                    session.getViewer()
            )));

            return item;
        }

        @Override
        public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            return ClickResult.changeMenu(SETTING_MENU);
        }
    }

    private static class AutoStoreSettingMenu implements Menu {

        private final List<Button> buttons = List.of(
                new ModeButton(), new BulkEditingButton(), new ToggleButton(), new DirectButton(), new BackOrCloseButton(22)
        );

        @Override
        public int getRows() {
            return 3;
        }

        @Override
        public @NotNull Component getTitle(@NotNull PlayerSession session) {
            return AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE;
        }

        @Override
        public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
            return buttons;
        }
    }
}
