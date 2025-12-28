package net.okocraft.box.feature.autostore.gui;

import dev.siroshun.mcmsgdef.MessageKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.AutoStoreSettingProvider;
import net.okocraft.box.feature.autostore.gui.buttons.BulkEditingButton;
import net.okocraft.box.feature.autostore.gui.buttons.DirectButton;
import net.okocraft.box.feature.autostore.gui.buttons.ModeButton;
import net.okocraft.box.feature.autostore.gui.buttons.ToggleButton;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoStoreClickMode implements BoxItemClickMode {

    private static final SoundBase ENABLE_SOUND = SoundBase.builder().sound(SoundEventKeys.BLOCK_WOODEN_BUTTON_CLICK_ON).pitch(1.5f).build();
    private static final SoundBase DISABLE_SOUND = SoundBase.builder().sound(SoundEventKeys.BLOCK_WOODEN_BUTTON_CLICK_OFF).pitch(1.5f).build();

    private final AutoStoreSettingProvider container;

    private final MessageKey displayName;
    private final MessageKey itemEnabled;
    private final MessageKey itemDisabled;
    private final MessageKey settingMenuButtonDisplayName;
    private final AutoStoreSettingMenu settingMenu;

    public AutoStoreClickMode(@NotNull AutoStoreSettingProvider container, @NotNull DefaultMessageCollector collector) {
        this.container = container;
        this.displayName = MessageKey.key(collector.add("box.autostore.gui.mode.display-name", "Auto-store setting"));
        this.itemEnabled = MessageKey.key(collector.add("box.autostore.gui.mode.item.enabled", "<gray>Auto-store setting: <green>Enabled"));
        this.itemDisabled = MessageKey.key(collector.add("box.autostore.gui.mode.item.disabled", "<gray>Auto-store setting: <red>Disabled"));
        this.settingMenuButtonDisplayName = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.open-button", "<gray>Open auto-store setting menu"));
        this.settingMenu = new AutoStoreSettingMenu(collector);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.LEVER;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull PlayerSession session) {
        return this.displayName.asComponent();
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) {
            return new ItemStack(Material.AIR);
        } else {
            var icon = item.getClonedItem();
            return ItemEditor.create()
                .copyLoreFrom(icon)
                .loreEmptyLine()
                .loreLine(setting.getPerItemModeSetting().isEnabled(item) ? this.itemEnabled : this.itemDisabled)
                .loreEmptyLine()
                .applyTo(session.getViewer(), icon);
        }
    }

    @Override
    public @NotNull ClickResult onSelect(@NotNull PlayerSession session) {
        var setting = this.container.getIfLoaded(session.getSourceUser().getUUID());
        if (setting != null) {
            session.putData(AutoStoreSetting.KEY, setting);
        }
        return ClickResult.UPDATE_ICONS;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        var playerSetting = session.getData(AutoStoreSetting.KEY);

        if (playerSetting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        var perItemSetting = playerSetting.getPerItemModeSetting();

        var enabled = !perItemSetting.isEnabled(item);

        perItemSetting.setEnabled(item, enabled);

        playerSetting.setEnabled(true);
        playerSetting.setAllMode(false);

        (enabled ? ENABLE_SOUND : DISABLE_SOUND).play(session.getViewer());

        return ClickResult.UPDATE_BUTTON;
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        if (session.getViewer().hasPermission("box.autostore")) {
            return this.container.isLoaded(session.getSourceUser().getUUID());
        } else {
            return false;
        }
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new AutoStoreSettingMenuButton(this.settingMenuButtonDisplayName, this.settingMenu, slot);
    }

    private record AutoStoreSettingMenuButton(@NotNull MessageKey displayName,
                                              @NotNull AutoStoreSettingMenu menu,
                                              int slot) implements Button {

        @Override
        public int getSlot() {
            return this.slot;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            return ItemEditor.create()
                .displayName(this.displayName)
                .createItem(session.getViewer(), Material.SUNFLOWER);
        }

        @Override
        public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            return ClickResult.changeMenu(this.menu);
        }
    }

    private static class AutoStoreSettingMenu implements Menu {

        private final MessageKey title;
        private final List<Button> buttons;

        private AutoStoreSettingMenu(@NotNull DefaultMessageCollector collector) {
            this.title = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.title", "<black>Auto-store Settings"));
            this.buttons = List.of(
                new ModeButton(collector),
                new BulkEditingButton(collector),
                new ToggleButton(collector),
                new DirectButton(collector),
                new BackOrCloseButton(22)
            );
        }

        @Override
        public int getRows() {
            return 3;
        }

        @Override
        public @NotNull Component getTitle(@NotNull PlayerSession session) {
            return this.title.asComponent();
        }

        @Override
        public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
            return this.buttons;
        }
    }
}
