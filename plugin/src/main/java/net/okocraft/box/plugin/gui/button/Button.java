package net.okocraft.box.plugin.gui.button;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface Button {

    @NotNull
    ButtonIcon getIcon();

    void setIcon(ButtonIcon icon);

    void onClick(@NotNull InventoryClickEvent e);

    void update();
}
