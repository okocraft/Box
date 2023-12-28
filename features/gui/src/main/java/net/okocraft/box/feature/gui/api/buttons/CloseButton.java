package net.okocraft.box.feature.gui.api.buttons;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record CloseButton(int slot) implements Button {

    private static final SoundBase CLOSE_SOUND = SoundBase.builder().sound(Sound.BLOCK_CHEST_CLOSE).pitch(1.5f).build();
    private static final MiniMessageBase DISPLAY_NAME = MiniMessageBase.messageKey(DisplayKeys.CLOSE);

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ItemEditor.create().displayName(DISPLAY_NAME.create(session.getMessageSource())).createItem(Material.OAK_DOOR);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        return close(session);
    }

    static @NotNull ClickResult.WaitingTask close(@NotNull PlayerSession session) {
        var clicker = session.getViewer();
        var result = ClickResult.waitingTask();

        BoxAPI.api().getScheduler().runEntityTask(clicker, () -> {
            clicker.closeInventory();
            CLOSE_SOUND.play(clicker);
            result.completeAsync(ClickResult.NO_UPDATE_NEEDED);
        });

        return result;
    }
}
