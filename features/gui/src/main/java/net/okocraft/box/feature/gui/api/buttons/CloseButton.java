package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloseButton implements Button {

    private static final SoundBase CLOSE_SOUND = SoundBase.builder().sound(Sound.BLOCK_CHEST_CLOSE).pitch(1.5f).build();

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.OAK_DOOR;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.CLOSE_BUTTON, viewer));
        return target;
    }

    @Override
    public int getSlot() {
        return 49;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        BoxProvider.get().getTaskFactory().runEntityTask(clicker, Player::closeInventory);
        CLOSE_SOUND.play(clicker);
    }
}
