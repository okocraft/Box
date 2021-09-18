package net.okocraft.box.feature.gui.api.menu;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

class RenderedButtonImpl implements RenderedButton {

    private final Button source;
    private final ItemStack icon;

    RenderedButtonImpl(@NotNull Button source) {
        this.source = source;
        this.icon = new ItemStack(source.getIconMaterial());
    }

    @Override
    public int getSlot() {
        return source.getSlot();
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return icon;
    }

    @Override
    public boolean shouldUpdate() {
        return source instanceof RefreshableButton;
    }

    @Override
    public void updateIcon(@NotNull Player viewer) {
        if (icon.getType() != source.getIconMaterial()) {
            icon.setType(source.getIconMaterial());
        }

        icon.setAmount(source.getIconAmount());

        var meta = icon.getItemMeta();

        if (meta != null) {
            meta = source.applyIconMeta(viewer, meta);
        }

        icon.setItemMeta(meta);
    }

    @Override
    public void clickButton(@NotNull Player clicker, @NotNull ClickType clickType) {
        source.onClick(clicker, clickType);
    }
}
