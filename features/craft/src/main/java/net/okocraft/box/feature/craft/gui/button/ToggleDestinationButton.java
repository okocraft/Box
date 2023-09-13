package net.okocraft.box.feature.craft.gui.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.gui.util.ItemCrafter;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleDestinationButton implements Button {

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
        var icon = new ItemStack(currentState ? Material.PLAYER_HEAD : Material.CHEST);

        icon.editMeta(target -> {
            var viewer = session.getViewer();
            target.displayName(TranslationUtil.render(Displays.DISTRIBUTION_BUTTON_DISPLAY_NAME, viewer));

            target.lore(List.of(
                    Component.empty(),
                    TranslationUtil.render(
                            Displays.DISTRIBUTION_CURRENT.apply(currentState), viewer
                    ),
                    Component.empty(),
                    TranslationUtil.render(
                            Displays.DISTRIBUTION_CLICK_TO_CHANGE.apply(currentState), viewer
                    ),
                    Component.empty()
            ));

        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        if (session.removeData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY) == null) {
            session.putData(ItemCrafter.PUT_CRAFTED_ITEMS_INTO_INVENTORY, Boolean.TRUE);
        }

        var clicker = session.getViewer();
        clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        return ClickResult.UPDATE_BUTTON;
    }
}
