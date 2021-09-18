package net.okocraft.box.feature.gui.internal.button;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class BoxItemButton implements RefreshableButton {

    private final BoxItem item;
    private final int slot;
    private final Supplier<BoxItemClickMode> clickModeSupplier;
    private final Menu menu;

    public BoxItemButton(@NotNull BoxItem item, int slot,
                         @NotNull Supplier<BoxItemClickMode> clickModeSupplier,
                         @Nullable Menu menu) {
        this.item = item;
        this.slot = slot;
        this.clickModeSupplier = clickModeSupplier;
        this.menu = menu;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return item.getOriginal().getType();
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var itemMeta = item.getOriginal().getItemMeta();

        if (itemMeta != null) {
            clickModeSupplier.get().applyIconMeta(viewer, item, itemMeta);
        }

        return itemMeta;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        clickModeSupplier.get().onClick(new BoxItemClickMode.Context(clicker, item, clickType, menu));
    }
}
