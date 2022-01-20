package net.okocraft.box.feature.gui.api.mode;

import java.util.Set;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BoxItemClickMode {

    @NotNull String getName();

    @NotNull Material getIconMaterial();

    @NotNull Component getDisplayName();

    void onClick(@NotNull Context context);

    void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target);

    boolean hasAdditionalButton();

    @NotNull AdditionalButton createAdditionalButton(@NotNull Player viewer, @NotNull Menu currentMenu);

    Set<GuiType> getApplicableGuiTypes();

    record Context(@NotNull Player clicker, @NotNull BoxItem item,
                   @NotNull ClickType clickType, @Nullable Menu currentMenu) {
    }

    enum GuiType {
        BE, JAVA;
    }
}
