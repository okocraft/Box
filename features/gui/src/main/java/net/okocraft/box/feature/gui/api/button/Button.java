package net.okocraft.box.feature.gui.api.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Button {

    static @NotNull Button empty(@NotNull Material material, int slot) {
        return new EmptyButton(material, slot);
    }

    @NotNull Material getIconMaterial();

    int getIconAmount();

    @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target);

    int getSlot();

    void onClick(@NotNull Player clicker, @NotNull ClickType clickType);

}
