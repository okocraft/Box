package net.okocraft.box.feature.craft.menu;

import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ResultItemIcon(@NotNull ItemStack item, int slot) implements RenderedButton {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull
    ItemStack getIcon() {
        return item;
    }

    @Override
    public boolean shouldUpdate() {
        return false;
    }

    @Override
    public void updateIcon(@NotNull Player viewer) {
    }

    @Override
    public void clickButton(@NotNull Player clicker, @NotNull ClickType clickType) {
    }
}
