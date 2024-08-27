package net.okocraft.box.feature.gui.api.button;

import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.util.XmasChecker;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Button {

    static @NotNull Button glassPane(int slot) {
        Material material;

        if (XmasChecker.isXmas()) {
            material = slot % 2 == 0 ?
                Material.GREEN_STAINED_GLASS_PANE :
                Material.RED_STAINED_GLASS_PANE;
        } else {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }

        return new EmptyButton(material, slot);
    }

    int getSlot();

    @NotNull ItemStack createIcon(@NotNull PlayerSession session);

    @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType);

}
