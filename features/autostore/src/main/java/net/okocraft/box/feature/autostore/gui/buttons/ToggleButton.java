package net.okocraft.box.feature.autostore.gui.buttons;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToggleButton extends AbstractAutoStoreSettingButton {

    private final MessageKey enabled;
    private final MessageKey disabled;
    private final MessageKey clickToEnable;
    private final MessageKey clickToDisable;

    public ToggleButton(@NotNull DefaultMessageCollector collector) {
        super(13);
        this.enabled = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.enabled", "<green>Enabled"));
        this.disabled = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.disabled", "<red>Disabled"));
        this.clickToEnable = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.click-to-enable", "<gray>Click to <green>enable<gray> auto store."));
        this.clickToDisable = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.click-to-disable", "<gray>Click to <red>disable<gray> auto store"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
            .displayName((setting.isEnabled() ? this.enabled : this.disabled))
            .loreEmptyLine()
            .loreLine((setting.isEnabled() ? this.clickToDisable : this.clickToEnable))
            .loreEmptyLine()
            .createItem(session.getViewer(), setting.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSetting.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        setting.setEnabled(!setting.isEnabled());
        SoundBase.CLICK.play(session.getViewer());
        this.callAutoStoreSettingChangeEvent(setting);

        return ClickResult.UPDATE_BUTTON;
    }
}
