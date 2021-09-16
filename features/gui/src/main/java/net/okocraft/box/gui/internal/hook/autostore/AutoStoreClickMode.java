package net.okocraft.box.gui.internal.hook.autostore;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.SettingManager;
import net.okocraft.box.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AutoStoreClickMode implements BoxItemClickMode {

    private final SettingManager settingManager;

    AutoStoreClickMode(@NotNull SettingManager settingManager) {
        this.settingManager = settingManager;
    }

    @Override
    public @NotNull String getName() {
        return "autostore";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.AUTOSTORE_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        var player = context.clicker();

        var playerSetting = settingManager.get(player);
        var perItemSetting = playerSetting.getPerItemModeSetting();

        var enabled = !perItemSetting.isEnabled(context.item());

        perItemSetting.setEnabled(context.item(), enabled);

        playerSetting.setMode(perItemSetting);

        var sound = enabled ? Sound.BLOCK_WOODEN_BUTTON_CLICK_ON : Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF;
        player.playSound(player.getLocation(), sound, 100f, 1.5f);
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var result = new ArrayList<Component>();

        var original = target.lore();

        if (original != null) {
            result.addAll(original);
        }

        result.add(Component.empty());

        var enabled = settingManager.get(viewer).getPerItemModeSetting().isEnabled(item);

        result.add(TranslationUtil.render(Displays.AUTOSTORE_MODE_LORE.apply(enabled), viewer));

        result.add(Component.empty());

        target.lore(result);
    }
}
