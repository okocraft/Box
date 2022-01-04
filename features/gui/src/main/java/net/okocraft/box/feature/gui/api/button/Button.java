package net.okocraft.box.feature.gui.api.button;

import net.okocraft.box.feature.gui.internal.util.XmasChecker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        return empty(material, slot);
    }

    static @NotNull Button empty(@NotNull Material material, int slot) {
        return new EmptyButton(material, slot);
    }

    @NotNull Material getIconMaterial();

    int getIconAmount();

    @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target);

    int getSlot();

    void onClick(@NotNull Player clicker, @NotNull ClickType clickType);

}
