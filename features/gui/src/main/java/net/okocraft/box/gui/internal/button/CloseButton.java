package net.okocraft.box.gui.internal.button;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.gui.api.button.Button;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CloseButton implements Button {

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
        CompletableFuture.runAsync(
                clicker::closeInventory, BoxProvider.get().getExecutorProvider().getMainThread()
        );

        clicker.playSound(clicker.getLocation(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.MASTER, 100f, 1.5f);
    }
}
