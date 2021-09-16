package net.okocraft.box.gui.api.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.gui.api.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public interface BoxItemClickMode {

    @NotNull String getName();

    @NotNull Component getDisplayName();

    void onClick(@NotNull Context context);

    void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target);

    boolean hasSettingMenu();

    @NotNull SettingMenuButton createSettingMenuButton(@NotNull Player viewer, @NotNull Menu currentMenu);

    record Context(@NotNull Player clicker, @NotNull BoxItem item, @NotNull ClickType clickType) {
    }
}
