package net.okocraft.box.feature.autostore.gui.buttons;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class BulkEditingButton extends AbstractAutoStoreSettingButton {

    private static final SoundBase ENABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_OPEN).pitch(1.5f).build();
    private static final SoundBase DISABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_CLOSE).pitch(1.5f).build();

    private static final TypedKey<Boolean> RECENT_OPERATION_KEY = TypedKey.of(Boolean.class, "autostore_bulk_editing_recent");

    private final MiniMessageBase displayName;
    private final MiniMessageBase clickToEnableAll;
    private final MiniMessageBase clickToDisableAll;
    private final MiniMessageBase doubleClickToEnableAll;
    private final MiniMessageBase doubleClickToDisableAll;
    private final MiniMessageBase recentAllEnabled;
    private final MiniMessageBase recentAllDisabled;

    public BulkEditingButton(@NotNull DefaultMessageCollector collector) {
        super(11);
        this.displayName = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.display-name", "<gold>Bulk change operation for per-item mode"));
        this.clickToEnableAll = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.click-to-enable-all", "<gray>Click to toggle all items to <green>enabled<gray>"));
        this.clickToDisableAll = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.click-to-disable-all", "<gray>Click to toggle all items to <red>disabled<gray>"));
        this.doubleClickToEnableAll = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.double-click-to-enable-all", "<gray>Double-click to toggle all items to <green>enabled<gray>"));
        this.doubleClickToDisableAll = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.double-click-to-disable-all", "<gray>Double-click to toggle all items to <red>disabled<gray>"));
        this.recentAllEnabled = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.recently-enabled", "<gray>Recent operation: <green>All enabled"));
        this.recentAllDisabled = messageKey(collector.add("box.autostore.gui.mode.setting-menu.buttons.bulk-edit.recently-disabled", "<gray>Recent operation: <red>All disabled"));
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var src = session.getMessageSource();
        Boolean recent = session.getData(RECENT_OPERATION_KEY);
        boolean nextClick = recent == null || !recent;

        return ItemEditor.create()
                .displayName(this.displayName.create(src))
                .loreEmptyLine()
                .loreLine((nextClick ? this.clickToEnableAll : this.clickToDisableAll).create(src))
                .loreLine((!nextClick ? this.doubleClickToEnableAll : this.doubleClickToDisableAll).create(src))
                .loreEmptyLine()
                .loreLineIf(recent != null, () -> (Boolean.TRUE.equals(recent) ? this.recentAllEnabled : this.recentAllDisabled).create(src))
                .loreEmptyLineIf(recent != null)
                .createItem(Material.TRIPWIRE_HOOK);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var setting = session.getData(AutoStoreSettingKey.KEY);

        if (setting == null) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        var perItemSetting = setting.getPerItemModeSetting();
        SoundBase sound;

        Boolean recent = session.getData(RECENT_OPERATION_KEY);

        if (recent == null || !recent) {
            perItemSetting.setEnabledItems(BoxAPI.api().getItemManager().getItemList());
            sound = ENABLE_ALL_SOUND;
            recent = true;
        } else {
            perItemSetting.setEnabledItems(Collections.emptyList());
            sound = DISABLE_ALL_SOUND;
            recent = false;
        }

        session.putData(RECENT_OPERATION_KEY, recent);
        var result = ClickResult.UPDATE_BUTTON;

        if (!setting.isEnabled()) {
            setting.setEnabled(true);
            result = ClickResult.UPDATE_ICONS;
        }

        if (setting.isAllMode()) {
            setting.setAllMode(false);
            result = ClickResult.UPDATE_ICONS;
        }

        sound.play(session.getViewer());
        callAutoStoreSettingChangeEvent(setting);

        return result;
    }
}
