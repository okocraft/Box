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

public class ModeButton extends AbstractAutoStoreSettingButton {

    private final MessageKey displayName;
    private final MessageKey changeToAll;
    private final MessageKey changeToPerItem;

    public ModeButton(@NotNull DefaultMessageCollector collector) {
        super(10);
        this.displayName = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.display-name", "<gold>Change auto-store mode"));
        this.changeToAll = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.click-to-all", "<gray>Click to change to <aqua>all-items"));
        this.changeToPerItem = MessageKey.key(collector.add("box.autostore.gui.mode.setting-menu.buttons.change-mode.click-to-per-item", "<gray>Click to change to <aqua>per-item"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        AutoStoreSetting setting = session.getData(AutoStoreSetting.KEY);
        if (setting == null) return new ItemStack(Material.AIR);

        return ItemEditor.create()
            .displayName(this.displayName)
            .loreEmptyLine()
            .loreLine((setting.isAllMode() ? this.changeToPerItem : this.changeToAll))
            .loreEmptyLine()
            .createItem(session.getViewer(), setting.isAllMode() ? Material.REDSTONE_TORCH : Material.SOUL_TORCH);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        AutoStoreSetting setting = session.getData(AutoStoreSetting.KEY);

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
        this.callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
