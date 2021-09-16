package net.okocraft.box.gui.api.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Menu {

    int getRows();

    @NotNull Component getTitle();

    void applyIcons(@NotNull ItemStack[] target);

    void clickMenu(@NotNull Player clicker, int slot, @NotNull ClickType clickType);

    boolean shouldUpdate();

    boolean isUpdated();

    void updateMenu(@NotNull Player viewer);

    default void onOpen(@NotNull Player viewer) {
    }
}
