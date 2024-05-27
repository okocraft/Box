package net.okocraft.box.feature.autostore.gui.buttons;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
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

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class ToggleButton extends AbstractAutoStoreSettingButton {

    private final MiniMessageBase enabled;
    private final MiniMessageBase disabled;
    private final MiniMessageBase clickToEnable;
    private final MiniMessageBase clickToDisable;

    public ToggleButton(@NotNull DefaultMessageCollector collector) {
        super(13);
        this.enabled = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.enabled", "<green>Enabled"));
        this.disabled = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.disabled", "<red>Disabled"));
        this.clickToEnable = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.click-to-enable", "<gray>Click to <green>enable<gray> auto store."));
        this.clickToDisable = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-autostore.click-to-disable", "<gray>Click to <red>disable<gray> auto store"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
                .displayName((setting.isEnabled() ? this.enabled : this.disabled).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine((setting.isEnabled() ? this.clickToDisable : this.clickToEnable).create(session.getMessageSource()))
                .loreEmptyLine()
                .createItem(setting.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL);
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
