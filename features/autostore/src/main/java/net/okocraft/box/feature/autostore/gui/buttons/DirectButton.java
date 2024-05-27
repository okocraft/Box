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

public class DirectButton extends AbstractAutoStoreSettingButton {

    private final MiniMessageBase displayName;
    private final MiniMessageBase enable;
    private final MiniMessageBase disable;

    public DirectButton(@NotNull DefaultMessageCollector collector) {
        super(15);
        this.displayName = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.display-name", "<gold>Toggle auto-store direct mode"));
        this.enable = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.click-to-enable", "<gray>Click to <aqua>enable<gray> auto-store direct"));
        this.disable = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.click-to-disable", "<gray>Click to <red>disable<gray> auto-store direct"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
                .displayName(this.displayName.create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine((setting.isDirect() ? this.disable : this.enable).create(session.getMessageSource()))
                .loreEmptyLine()
                .createItem(Material.HOPPER);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSetting.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        ClickResult result;

        setting.setDirect(!setting.isDirect());

        if (setting.isDirect() && !setting.isEnabled()) {
            setting.setEnabled(true);
            result = ClickResult.UPDATE_ICONS;
        } else {
            result = ClickResult.UPDATE_BUTTON;
        }

        SoundBase.CLICK.play(session.getViewer());
        this.callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
