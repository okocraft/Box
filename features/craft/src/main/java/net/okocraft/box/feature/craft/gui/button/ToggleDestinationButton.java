package net.okocraft.box.feature.craft.gui.button;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.okocraft.box.feature.craft.gui.util.ItemCrafter;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToggleDestinationButton implements Button {

    private static final MiniMessageBase INVENTORY = MiniMessageBase.messageKey(DisplayKeys.INVENTORY);
    private static final MiniMessageBase BOX = MiniMessageBase.messageKey(DisplayKeys.BOX);
    private static final Arg1<Boolean> DISPLAY_NAME = Arg1.arg1(DisplayKeys.DESTINATION_BUTTON, Placeholder.messageBase("destination", state -> state ? INVENTORY: BOX));
    private static final MiniMessageBase CHANGE_TO_INVENTORY = MiniMessageBase.messageKey(DisplayKeys.CHANGE_TO_INVENTORY);
    private static final MiniMessageBase CHANGE_TO_BOX = MiniMessageBase.messageKey(DisplayKeys.CHANGE_TO_BOX);

    private final int slot;

    public ToggleDestinationButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        boolean currentState = session.getData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY) != null;
        return ItemEditor.create()
                .displayName(DISPLAY_NAME.apply(currentState).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine((currentState ? CHANGE_TO_BOX : CHANGE_TO_INVENTORY).create(session.getMessageSource()))
                .loreEmptyLine()
                .createItem(currentState ? Material.PLAYER_HEAD : Material.CHEST);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (session.removeData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY) == null) {
            session.putData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY, Boolean.TRUE);
        }

        SoundBase.CLICK.play(session.getViewer());
        return ClickResult.UPDATE_BUTTON;
    }
}
