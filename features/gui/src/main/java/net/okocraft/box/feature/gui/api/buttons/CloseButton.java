package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record CloseButton(int slot) implements Button {

    private static final SoundBase CLOSE_SOUND = SoundBase.builder().sound(Sound.BLOCK_CHEST_CLOSE).pitch(1.5f).build();

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.OAK_DOOR);

        icon.editMeta(meta -> meta.displayName(TranslationUtil.render(Displays.CLOSE_BUTTON, session.getViewer())));

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        return close(session);
    }

    static @NotNull ClickResult.WaitingTask close(@NotNull PlayerSession session) {
        var clicker = session.getViewer();
        var result = ClickResult.waitingTask();

        BoxProvider.get().getScheduler().runEntityTask(clicker, () -> {
            clicker.closeInventory();
            CLOSE_SOUND.play(clicker);
            result.completeAsync(ClickResult.NO_UPDATE_NEEDED);
        });

        return result;
    }
}
