package net.okocraft.box.feature.autostore.gui.buttons;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class ModeButton extends AbstractAutoStoreSettingButton {

    private final MiniMessageBase displayName;
    private final MiniMessageBase changeToAll;
    private final MiniMessageBase changeToPerItem;

    public ModeButton(@NotNull DefaultMessageCollector collector) {
        super(10);
        this.displayName = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.display-name", "<gold>Change auto-store mode"));
        this.changeToAll = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.click-to-all", "<gray>Click to change to <aqua>all-items"));
        this.changeToPerItem = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.click-to-per-item", "<gray>Click to change to <aqua>per-item"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var setting = session.getData(AutoStoreSettingKey.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
                .displayName(this.displayName.create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine((setting.isAllMode() ? this.changeToPerItem : this.changeToAll).create(session.getMessageSource()))
                .loreEmptyLine()
                .createItem(setting.isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        ClickResult result;
        setting.setAllMode(!setting.isAllMode());

        if (!setting.isEnabled()) {
            setting.setEnabled(true);
            result = ClickResult.UPDATE_ICONS;
        } else {
            result = ClickResult.UPDATE_BUTTON;
        }

        SoundBase.CLICK.play(session.getViewer());
        callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
