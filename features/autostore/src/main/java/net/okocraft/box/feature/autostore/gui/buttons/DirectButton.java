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

public class DirectButton extends AbstractAutoStoreSettingButton {

    private final MessageKey displayName;
    private final MessageKey enable;
    private final MessageKey disable;

    public DirectButton(@NotNull DefaultMessageCollector collector) {
        super(15);
        this.displayName = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.display-name", "<gold>Toggle auto-store direct mode"));
        this.enable = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.click-to-enable", "<gray>Click to <aqua>enable<gray> auto-store direct"));
        this.disable = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.toggle-direct.click-to-disable", "<gray>Click to <red>disable<gray> auto-store direct"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
            .displayName(this.displayName)
            .loreEmptyLine()
            .loreLine((setting.isDirect() ? this.disable : this.enable))
            .loreEmptyLine()
            .createItem(session.getViewer(), Material.HOPPER);
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
