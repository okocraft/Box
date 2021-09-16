package net.okocraft.box.gui.api.menu;

import net.okocraft.box.gui.api.button.Button;
import net.okocraft.box.gui.api.button.RefreshableButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderedButton {

    private final Button source;
    private final ItemStack icon;

    public RenderedButton(@NotNull Button source) {
        this.source = source;
        this.icon = new ItemStack(source.getIconMaterial());
    }

    public int getSlot() {
        return source.getSlot();
    }

    public @NotNull ItemStack getIcon() {
        return icon;
    }

    public boolean shouldUpdate() {
        return source instanceof RefreshableButton;
    }

    public void updateIcon(@NotNull Player viewer) {
        icon.setAmount(source.getIconAmount());

        var meta = icon.getItemMeta();

        if (meta != null) {
            meta = source.applyIconMeta(viewer, meta);
        }

        icon.setItemMeta(meta);
    }

    public void clickButton(@NotNull Player clicker, @NotNull ClickType clickType) {
        source.onClick(clicker, clickType);
    }
}
