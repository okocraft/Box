package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.gui.AutoStoreSettingKey;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class BulkEditingButton extends AbstractAutoStoreSettingButton {

    private static final SoundBase ENABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_OPEN).pitch(1.5f).build();
    private static final SoundBase DISABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_CLOSE).pitch(1.5f).build();

    private static final TypedKey<Boolean> RECENT_OPERATION_KEY = TypedKey.of(Boolean.class, "autostore_bulk_editing_recent");

    public BulkEditingButton() {
        super(11);
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var item = new ItemStack(Material.TRIPWIRE_HOOK);
        item.editMeta(meta -> editIconMeta(session, meta));
        return item;
    }

    private void editIconMeta(@NotNull PlayerSession session, @NotNull ItemMeta target) {
        var displayName = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE;
        target.displayName(TranslationUtil.render(displayName, session.getViewer()));

        Boolean recent = session.getData(RECENT_OPERATION_KEY);

        var lore = new ArrayList<Component>();

        lore.add(Component.empty());

        boolean nextClick = recent == null || !recent;

        lore.add(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_CLICK.apply(nextClick));
        lore.add(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_DOUBLE_CLICK.apply(!nextClick));
        lore.add(Component.empty());

        if (recent != null) {
            lore.add(AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RECENT.apply(recent));
            lore.add(Component.empty());
        }

        target.lore(TranslationUtil.render(lore, session.getViewer()));
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
            perItemSetting.setEnabledItems(BoxProvider.get().getItemManager().getItemList());
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
