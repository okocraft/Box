package net.okocraft.box.feature.gui.api.menu;

import net.okocraft.box.feature.gui.api.button.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface RenderedButton {

    static @NotNull RenderedButton create(@NotNull Button source) {
        return new RenderedButtonImpl(source);
    }

    int getSlot();

    @NotNull ItemStack getIcon();

    boolean shouldUpdate();

    void updateIcon(@NotNull Player viewer);

    void clickButton(@NotNull Player clicker, @NotNull ClickType clickType);
}
