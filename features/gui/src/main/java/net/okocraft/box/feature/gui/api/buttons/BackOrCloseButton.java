package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record BackOrCloseButton(int slot) implements Button {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.OAK_DOOR);

        var display = session.hasPreviousMenu() ? Displays.BACK_BUTTON : Displays.CLOSE_BUTTON;
        icon.editMeta(meta -> meta.displayName(TranslationUtil.render(display, session.getViewer())));

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var clicker = session.getViewer();

        clicker.playSound(clicker.getLocation(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.MASTER, 100f, 1.5f);

        if (session.hasPreviousMenu()) {
            return ClickResult.changeMenu(session.backMenu());
        } else {
            var result = ClickResult.waitingTask();

            BoxProvider.get().getScheduler().runEntityTask(clicker, () -> {
                clicker.closeInventory();
                result.completeAsync(ClickResult.NO_UPDATE_NEEDED);
            });

            return result;
        }
    }
}
