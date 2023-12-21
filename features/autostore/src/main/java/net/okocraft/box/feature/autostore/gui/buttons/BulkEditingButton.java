package net.okocraft.box.feature.autostore.gui.buttons;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.gui.AutoStoreMenuDisplays;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class BulkEditingButton extends AbstractAutoStoreSettingButton {

    private static final SoundBase ENABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_OPEN).pitch(1.5f).build();
    private static final SoundBase DISABLE_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_DOOR_CLOSE).pitch(1.5f).build();

    private Boolean recent = null;

    public BulkEditingButton(@NotNull AutoStoreSetting setting) {
        super(11, setting);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.TRIPWIRE_HOOK;
    }


    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var displayName = AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE;
        target.displayName(TranslationUtil.render(displayName, viewer));

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

        target.lore(TranslationUtil.render(lore, viewer));

        return target;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var perItemSetting = setting.getPerItemModeSetting();
        SoundBase sound;

        if (recent == null || !recent) {
            perItemSetting.setEnabledItems(BoxProvider.get().getItemManager().getItemList());
            sound = ENABLE_ALL_SOUND;
            recent = true;
        } else {
            perItemSetting.setEnabledItems(Collections.emptyList());
            sound = DISABLE_ALL_SOUND;
            recent = false;
        }

        if (!setting.isEnabled()) {
            setting.setEnabled(true);
        }

        if (setting.isAllMode()) {
            setting.setAllMode(false);
        }

        sound.play(clicker);
        callAutoStoreSettingChangeEvent();
    }
}
