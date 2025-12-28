package net.okocraft.box.feature.craft.gui.button;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.minimessage.translation.Argument;
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

    private static final MessageKey INVENTORY = MessageKey.key(DisplayKeys.INVENTORY);
    private static final MessageKey BOX = MessageKey.key(DisplayKeys.BOX);
    private static final MessageKey.Arg1<Boolean> DISPLAY_NAME = MessageKey.arg1(DisplayKeys.DESTINATION_BUTTON, state -> Argument.component("destination", state ? INVENTORY : BOX));
    private static final MessageKey CHANGE_TO_INVENTORY = MessageKey.key(DisplayKeys.CHANGE_TO_INVENTORY);
    private static final MessageKey CHANGE_TO_BOX = MessageKey.key(DisplayKeys.CHANGE_TO_BOX);

    private final int slot;

    public ToggleDestinationButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        boolean currentState = session.getData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY) != null;
        return ItemEditor.create()
            .displayName(DISPLAY_NAME.apply(currentState))
            .loreEmptyLine()
            .loreLine(currentState ? CHANGE_TO_BOX : CHANGE_TO_INVENTORY)
            .loreEmptyLine()
            .createItem(session.getViewer(), currentState ? Material.PLAYER_HEAD : Material.CHEST);
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
