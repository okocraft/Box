package net.okocraft.box.feature.gui.api.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface BoxItemClickMode {

    @NotNull Material getIconMaterial();

    @NotNull Component getDisplayName(@NotNull PlayerSession session);

    @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item);

    @NotNull ClickResult onSelect(@NotNull PlayerSession session);

    @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType);

    boolean hasAdditionalButton();

    @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot);

    boolean canUse(@NotNull PlayerSession session);
}
